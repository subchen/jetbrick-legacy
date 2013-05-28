package jetbrick.web.mvc.router;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrick.commons.lang.CamelCaseUtils;
import jetbrick.web.mvc.*;
import jetbrick.web.mvc.config.WebappConfig;
import jetbrick.web.mvc.controller.*;
import org.apache.commons.lang3.StringUtils;

public abstract class SimpleRouter implements Router {
    protected Map<String, RouteInfo> routeCache = new HashMap<String, RouteInfo>();
    protected Map<String, Method> actionCache = new HashMap<String, Method>();

    protected boolean classNameCamelCaseEnabled;
    protected boolean methodNameCamelCaseEnabled;
    protected String controllerPattern = "*"; // webapps.controller.*Controller 

    public void setClassNameCamelCaseEnabled(boolean enabled) {
        this.classNameCamelCaseEnabled = enabled;
    }

    public void setMethodNameCamelCaseEnabled(boolean enabled) {
        this.methodNameCamelCaseEnabled = enabled;
    }

    public void setControllerPattern(String controllerPattern) {
        this.controllerPattern = controllerPattern;
    }

    @Override
    public RouteInfo getRouteInfo(HttpServletRequest request) {
        String uri = request.getServletPath();
        RouteInfo info = routeCache.get(uri);
        if (info == null) {
            info = lookupRouteInfo(request, uri);
            if (info == null) {
                info = RouteInfo.NOT_FOUND;
            }
            routeCache.put(uri, info);
        }
        return info;
    }

    protected abstract RouteInfo lookupRouteInfo(HttpServletRequest request, String uri);

    protected RouteInfoImpl lookupControllerClass(String packageName, String className, String methodName) {
        if (classNameCamelCaseEnabled) {
            className = CamelCaseUtils.toCapitalizeCamelCase(className);
        }
        if (methodNameCamelCaseEnabled) {
            methodName = CamelCaseUtils.toCamelCase(methodName);
        }

        try {
            String fullClassName = className;
            if (StringUtils.isNotEmpty(packageName)) {
                fullClassName = packageName + "." + className;
            }
            fullClassName = StringUtils.replace(controllerPattern, "*", fullClassName);

            Class<?> controllerClass = Class.forName(fullClassName);
            loadActionsInControllerClass(controllerClass);
            Method action = actionCache.get(fullClassName + "::" + methodName);
            if (action == null) return null;

            RouteInfoImpl info = new RouteInfoImpl();
            info.setControllerClass(controllerClass);
            info.setActionMethod(action);
            info.setBeforeMethod(actionCache.get(fullClassName + "::BEFORE"));
            info.setAfterMethod(actionCache.get(fullClassName + "::AFTER"));
            return info;
        } catch (ClassNotFoundException e) {
        }

        return null;
    }

    // load action into cache
    protected void loadActionsInControllerClass(Class<?> controllerClass) {
        String className = controllerClass.getName();
        if (actionCache.containsKey(className)) {
            if (!WebappConfig.getInstance().isDevelopmentMode()) {
                return;
            }
        }

        for (Method m : controllerClass.getMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
                Class<?>[] types = m.getParameterTypes();
                if (types.length == 1 && RequestContext.class.equals(types[0])) {
                    String key = className + "::" + m.getName();
                    if (m.getAnnotation(Before.class) != null) {
                        key = className + "::BEFORE";
                    } else if (m.getAnnotation(After.class) != null) {
                        key = className + "::AFTER";
                    }
                    actionCache.put(key, m);
                }
            }
        }
        actionCache.put(className, null);
    }
}

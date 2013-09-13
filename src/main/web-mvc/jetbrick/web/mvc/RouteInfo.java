package jetbrick.web.mvc;

import java.lang.reflect.Method;
import jetbrick.web.mvc.router.RouteInfoImpl;

public abstract class RouteInfo {
    public static RouteInfo NOT_FOUND = new RouteInfoImpl();

    protected Class<?> controllerClass;
    protected Method actionMethod;
    protected Method beforeMethod;
    protected Method afterMethod;
    protected String[] restfulParameters;
    protected String viewName;

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getActionMethod() {
        return actionMethod;
    }

    public Method getBeforeMethod() {
        return beforeMethod;
    }

    public Method getAfterMethod() {
        return afterMethod;
    }

    public String[] getRestfulParameters() {
        return restfulParameters;
    }

    public String getViewName() {
        return viewName;
    }
}

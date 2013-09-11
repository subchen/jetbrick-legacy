package jetbrick.web.mvc.intercept;

import java.lang.reflect.Method;
import java.util.List;
import jetbrick.web.mvc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 依次执行所有的Interceptor，完成后在执行action
 */
public class InterceptorChainImpl implements InterceptorChain {
    private static final Logger log = LoggerFactory.getLogger(InterceptorChainImpl.class);

    private final List<Interceptor> interceptors;
    private int currentIndex = 0;
    private Result result;

    public InterceptorChainImpl(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public void invork(RequestContext rc) throws Throwable {
        if (currentIndex < interceptors.size()) {
            Interceptor interceptor = interceptors.get(currentIndex++);
            interceptor.intercept(rc, this);
        } else {
            executeAction(rc);
        }
    }

    @Override
    public void handleResult(Object result) {
        this.result = Result.of(result);
    }

    public Result getResult() {
        return result;
    }

    private void executeAction(RequestContext rc) throws Throwable {
        RouteInfo route = rc.getRouteInfo();
        if (route == RouteInfo.NOT_FOUND) {
            log.warn("Controller not found, forward view directly.");
            result = Result.DEFAULT_VIEW;
        } else {
            //Using byte-code enhancement to dispatch action.
            //Controller controller = controllerClass.newInstance();
            //result = controller.__main__(rc);

            Class<?> controllerClass = route.getControllerClass();
            Object controller = controllerClass.newInstance();

            Method before = route.getBeforeMethod();
            if (before != null) {
                before.setAccessible(true); // 禁用访问安全检查, 提高Java反射速度
                before.invoke(controller, rc);
            }

            Method method = route.getActionMethod();
            method.setAccessible(true); // 禁用访问安全检查, 提高Java反射速度
            Object resultObject = method.invoke(controller, rc);
            Class<?> resultClass = method.getReturnType();
            result = Result.of(resultObject, resultClass);

            Method after = route.getAfterMethod();
            if (after != null) {
                after.setAccessible(true); // 禁用访问安全检查, 提高Java反射速度
                after.invoke(controller, rc);
            }
        }
    }

}

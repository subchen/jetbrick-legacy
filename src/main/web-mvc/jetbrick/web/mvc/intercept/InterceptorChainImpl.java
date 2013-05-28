package jetbrick.web.mvc.intercept;

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
            //Controller controller = controllerClass.newInstance();
            //result = controller.__main__(rc);

            Class<?> controllerClass = route.getControllerClass();
            Object controller = controllerClass.newInstance();

            if (route.getBeforeMethod() != null) {
                route.getBeforeMethod().invoke(controller, rc);
            }

            Object resultObject = route.getActionMethod().invoke(controller, rc);
            Class<?> resultClass = route.getActionMethod().getReturnType();
            result = Result.of(resultObject, resultClass);

            if (route.getAfterMethod() != null) {
                route.getAfterMethod().invoke(controller, rc);
            }
        }
    }

}

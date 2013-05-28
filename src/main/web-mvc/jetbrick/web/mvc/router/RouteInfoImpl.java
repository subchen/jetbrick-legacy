package jetbrick.web.mvc.router;

import java.lang.reflect.Method;
import jetbrick.web.mvc.RouteInfo;

public class RouteInfoImpl extends RouteInfo {
    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public void setActionMethod(Method actionMethod) {
        this.actionMethod = actionMethod;
    }

    public void setBeforeMethod(Method beforeMethod) {
        this.beforeMethod = beforeMethod;
    }

    public void setAfterMethod(Method afterMethod) {
        this.afterMethod = afterMethod;
    }

    public void setRestfulParameters(String[] restfulParameters) {
        this.restfulParameters = restfulParameters;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}

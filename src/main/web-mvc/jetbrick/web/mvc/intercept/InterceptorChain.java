package jetbrick.web.mvc.intercept;

import jetbrick.web.mvc.RequestContext;

public interface InterceptorChain {

    public void invork(RequestContext rc) throws Throwable;

    public void handleResult(Object result);

}

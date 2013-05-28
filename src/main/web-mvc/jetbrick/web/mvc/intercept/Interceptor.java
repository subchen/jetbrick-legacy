package jetbrick.web.mvc.intercept;

import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.config.WebappConfig;

public interface Interceptor {

    public void init(WebappConfig config);

    public void intercept(RequestContext ac, InterceptorChain chain) throws Throwable;

    public void destory();

}

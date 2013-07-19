package jetbrick.web.mvc;

import java.io.IOException;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.config.WebappConfigImpl;
import jetbrick.web.mvc.intercept.Interceptor;
import jetbrick.web.mvc.intercept.InterceptorChainImpl;
import jetbrick.web.mvc.multipart.FileUploaderUtils;
import jetbrick.web.mvc.plugin.Plugin;
import jetbrick.web.utils.ServletUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DispatcherFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(DispatcherFilter.class);

    private WebappConfigImpl config;
    private Router router;
    private ViewRender viewRender;
    private String encoding;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("DispatcherFilter starting ...");
        log.info("user.dir = " + System.getProperty("user.dir"));
        log.info("user.timezone = " + System.getProperty("user.timezone"));
        log.info("file.encoding = " + System.getProperty("file.encoding"));

        config = new WebappConfigImpl();
        config.init(filterConfig);

        encoding = config.getEncoding();
        router = config.getRouter();
        viewRender = config.getViewRender();

        log.info("router = " + router.getClass().getName());
        log.info("view.render = " + viewRender.getClass().getName());
        log.info("exception.handler = " + config.getExceptionHandler().getClass().getName());

        for (Plugin plugin : config.getPluginList()) {
            log.info("load plugin: " + plugin.getClass().getName());
            plugin.init(config);
        }
        for (Interceptor interceptor : config.getInterceptorList()) {
            log.info("load interceptor: " + interceptor.getClass().getName());
            interceptor.init(config);
        }

        log.info("development.mode = " + config.isDevelopmentMode());
        log.info("webapp.root = " + config.getWebappPath());
        log.info("DispatcherFilter loaded.");
    }

    @Override
    public void destroy() {
        log.info("DispatcherFilter destroy...");
        for (Interceptor interceptor : config.getInterceptorList()) {
            interceptor.destory();
        }
        for (Plugin plugin : config.getPluginList()) {
            plugin.destory();
        }
        log.info("DispatcherFilter exit.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);

        if (!router.accept(request)) {
            chain.doFilter(req, resp);
            return;
        }

        if (config.isCacheOff()) {
            ServletUtils.setBufferOff(response);
        }

        RouteInfo routeInfo = router.getRouteInfo(request);

        // support multipart request
        request = FileUploaderUtils.asRequest(request, SystemUtils.getJavaIoTmpDir());
        RequestContext rc = new RequestContext(config, request, response, routeInfo);
        try {
            List<Interceptor> interceptors = config.getInterceptorList();
            InterceptorChainImpl interceptorChain = new InterceptorChainImpl(interceptors);
            interceptorChain.invork(rc);

            Result result = interceptorChain.getResult();
            if (result != null) {
                result.render(rc, viewRender);
            }
        } catch (Throwable e) {
            ExceptionHandler handler = config.getExceptionHandler();
            if (handler == null) {
                throw SystemException.unchecked(e);
            }

            try {
                handler.handleError(e);
            } catch (IOException ex) {
                throw ex;
            } catch (ServletException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw SystemException.unchecked(ex);
            }
        } finally {
            rc.destory();
        }
    }

}

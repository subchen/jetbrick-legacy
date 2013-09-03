package jetbrick.web.mvc.config;

import java.io.File;
import java.util.List;
import jetbrick.web.mvc.*;
import jetbrick.web.mvc.intercept.Interceptor;
import jetbrick.web.mvc.plugin.Plugin;
import jetbrick.web.mvc.router.SimpleRailsRouter;
import jetbrick.web.mvc.view.DefaultViewRender;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.SystemUtils;

/**
 * 这是一个用户配置类，需要用户重载。
 */
public abstract class UserConfig {

    protected WebappConfig webappConfig;

    public void init(WebappConfig webappConfig) {
        this.webappConfig = webappConfig;
    }

    public WebappConfig getWebappConfig() {
        return webappConfig;
    }

    public String getEncoding() {
        return "utf-8";
    }

    public boolean isCacheOff() {
        return true;
    }

    public boolean isDevelopmentMode() {
        return false;
    }
    
    public File getUploadDirectory() {
        return SystemUtils.getJavaIoTmpDir();
    }

    public Router getRouter() {
        SimpleRailsRouter router = new SimpleRailsRouter();
        router.setControllerPattern("webapps.controller.*Controller");
        return router;
    }

    public ViewRender getViewRender() {
        DefaultViewRender viewRender = new DefaultViewRender();
        viewRender.setViewPattern("*.jsp");
        return viewRender;
    }

    public ExceptionHandler getExceptionHandler() {
        return null;
    }

    public void configInterceptors(List<Interceptor> interceptors) {
    }

    public void configPlugins(List<Plugin> plugins) {
    }

    public void configConfiguration(Configuration configuration) {
    }
}

package jetbrick.web.mvc.config;

import java.io.File;
import javax.servlet.ServletContext;
import org.apache.commons.configuration.Configuration;
import org.springframework.context.ApplicationContext;

/**
 * 这个 webapp 的全局配置类， 提供给用户使用，由WebappConfigImpl初始化
 */
public abstract class WebappConfig {
    private static WebappConfig defaultConfigInstance;

    protected File webappRoot;
    protected ServletContext servletContext;
    protected boolean developmentMode;
    protected Configuration configuration;
    protected Object springAppContext;
    protected String encoding;
    protected boolean cacheOff;

    protected WebappConfig() {
        defaultConfigInstance = this;
    }

    // 提供全局变量访问
    public static WebappConfig getInstance() {
        return defaultConfigInstance;
    }

    public File getWebappPath() {
        return webappRoot;
    }

    public File getWebappPath(String path) {
        return new File(webappRoot, path);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public String getEncoding() {
        return encoding;
    }

    public boolean isCacheOff() {
        return cacheOff;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ApplicationContext getSpringAppContext() {
        return (ApplicationContext) springAppContext;
    }
}

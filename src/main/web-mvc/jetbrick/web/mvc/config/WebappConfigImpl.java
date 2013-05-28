package jetbrick.web.mvc.config;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.*;
import jetbrick.web.mvc.intercept.Interceptor;
import jetbrick.web.mvc.plugin.Plugin;
import org.apache.commons.configuration.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class WebappConfigImpl extends WebappConfig {
    private static final Logger log = LoggerFactory.getLogger(WebappConfigImpl.class);

    private FilterConfig filterConfig;
    private UserConfig userConfig;
    private Router router;
    private ViewRender viewRender;
    private ExceptionHandler exceptionHandler;
    private MultipartResolver multipartResolver;
    private List<Interceptor> interceptors = new ArrayList<Interceptor>();
    private List<Plugin> plugins = new ArrayList<Plugin>();

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.servletContext = filterConfig.getServletContext();
        this.webappRoot = new File(servletContext.getRealPath("/"));

        lookupSpringAppContext();
        lookupMultipartResolver();
        lookupConfiguration();

        // load config from user
        lookupUserConfig();
    }

    private void lookupUserConfig() {
        String configClass = filterConfig.getInitParameter("config");
        if (StringUtils.isEmpty(configClass)) {
            throw new RuntimeException("Please set config parameter of DispatcherFilter in web.xml");
        }

        try {
            userConfig = (UserConfig) Class.forName(configClass).newInstance();
            userConfig.init(this);
            userConfig.configPlugins(plugins);
            userConfig.configInterceptors(interceptors);
            userConfig.configConfiguration(configuration);
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }

        encoding = userConfig.getEncoding();
        cacheOff = userConfig.isCacheOff();
        developmentMode = userConfig.isDevelopmentMode();

        exceptionHandler = userConfig.getExceptionHandler();
        router = userConfig.getRouter();
        viewRender = userConfig.getViewRender();
    }

    private void lookupSpringAppContext() {
        try {
            Class<?> clazz = Class.forName("org.springframework.web.context.support.WebApplicationContextUtils");
            Method m = clazz.getMethod("getWebApplicationContext", ServletContext.class);
            springAppContext = m.invoke(null, filterConfig.getServletContext());
            if (springAppContext == null) {
                log.warn("There isn't Spring ContextLoader* definitions in your web.xml!");
            }
        } catch (ClassNotFoundException e) {
            log.warn("spring*.jar can't be found in your classpath!");
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    private void lookupConfiguration() throws ServletException {
        String fileName = filterConfig.getServletContext().getRealPath("/WEB-INF/configuration.xml");
        if (new File(fileName).exists()) {
            try {
                ConfigurationBuilder factory = new DefaultConfigurationBuilder(fileName);
                configuration = factory.getConfiguration();

                log.info("using Configuration [" + fileName + "]");
            } catch (ConfigurationException e) {
                throw new ServletException(e);
            }
        } else {
            fileName = filterConfig.getServletContext().getRealPath("/WEB-INF/configuration.properties");
            if (new File(fileName).exists()) {
                try {
                    CombinedConfiguration cfg = new CombinedConfiguration();
                    cfg.addConfiguration(new SystemConfiguration());
                    cfg.addConfiguration(new PropertiesConfiguration(fileName));

                    configuration = cfg;
                    log.info("using Configuration [" + fileName + "]");
                } catch (ConfigurationException e) {
                    throw new ServletException(e);
                }
            } else {
                configuration = new SystemConfiguration();
                log.warn("/WEB-INF/configuration.xml or /WEB-INF/configuration.properties not found!");
            }
        }
    }

    private void lookupMultipartResolver() {
        try {
            Class.forName("org.apache.commons.fileupload.FileUpload");
            multipartResolver = new CommonsMultipartResolver();
            log.info("using commons-fileupload for file upload.");
        } catch (ClassNotFoundException e) {
            log.warn("commons-fileupload.jar not found! file upload capability does not support.");
        }
    }

    public Router getRouter() {
        return router;
    }

    public ViewRender getViewRender() {
        return viewRender;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public List<Interceptor> getInterceptorList() {
        return interceptors;
    }

    public List<Plugin> getPluginList() {
        return plugins;
    }

    public MultipartResolver getMultipartResolver() {
        return multipartResolver;
    }
}

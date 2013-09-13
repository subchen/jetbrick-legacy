package jetbrick.web.mvc.view;

import httl.Engine;
import httl.Template;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.config.WebappConfig;
import org.apache.commons.lang3.StringUtils;

public class HttlTemplateViewRender extends AbstractViewRender {
    private final Engine engine;

    public HttlTemplateViewRender() {
        this(new Properties());
    }

    public HttlTemplateViewRender(Properties config) {
        Properties properties = getDefaultProperties();
        properties.putAll(config);
        engine = Engine.getEngine(properties);

        viewPattern = "*.httl";
    }

    private Properties getDefaultProperties() {
        WebappConfig webappConfig = WebappConfig.getInstance();
        File webroot = webappConfig.getWebappPath();

        //@formatter:off
        String[] defaultVals = { 
            "javax.servlet.http.HttpServletRequest request",
            "javax.servlet.http.HttpServletResponse response",
            "javax.servlet.http.HttpSession session",
            "javax.servlet.ServletContext application",
            "java.util.Map parameters",
            "javax.servlet.http.Cookie[] cookies"
        };
        //@formatter:on

        Properties config = new Properties();
        config.setProperty("import.variables+", StringUtils.join(defaultVals, ","));

        config.setProperty("loaders", "httl.spi.loaders.FileLoader");
        config.setProperty("template.directory", webroot.getPath());
        config.setProperty("template.suffix", ".httl");

        config.setProperty("input.encoding", webappConfig.getEncoding());
        config.setProperty("output.encoding", webappConfig.getEncoding());

        config.setProperty("reloadable", webappConfig.isDevelopmentMode() ? "true" : "false");
        config.setProperty("precompiled", "false");
        config.setProperty("compiler", "httl.spi.compilers.JavassistCompiler");
        config.setProperty("loggers", "httl.spi.loggers.Slf4jLogger");

        config.setProperty("value.switchers", "");
        config.setProperty("value.filters", "");
        config.setProperty("text.filters", "");
        config.setProperty("json.codec", "httl.spi.codecs.FastjsonCodec");
        config.setProperty("remove.directive.blank.line", "true");

        config.setProperty("output.stream", "true");
        config.setProperty("output.writer", "false");

        config.setProperty("source.in.class", "false");
        config.setProperty("text.in.class", "false");
        return config;
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        Map<String, Object> context = getContext(rc);

        OutputStream out = rc.getResponse().getOutputStream();
        String layoutView = (String) context.get(layoutKey);
        if (layoutView == null) {
            // no layout 
            getTemplate(view).render(context, out);
        } else {
            // output main content 
            ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
            getTemplate(view).render(context, os);
            context.put(mainContentKey, os.toString(WebappConfig.getInstance().getEncoding()));

            // output layout 
            getTemplate(layoutView).render(context, out);
        }
        out.flush();
    }

    private Template getTemplate(String url) throws Throwable {
        Template template = engine.getTemplate(url);
        if (template == null) {
            throw new SystemException(HttpError.STATUS_404, "HTTL template not found: " + url);
        }
        return template;
    }

    private Map<String, Object> getContext(RequestContext rc) {
        HttpServletRequest request = rc.getRequest();
        Map<String, Object> context = rc.getAttributes();
        context.put("request", request);
        context.put("response", rc.getResponse());
        context.put("session", rc.getSession());
        context.put("application", rc.getServletContext());
        context.put("parameters", request.getParameterMap());
        context.put("cookies", request.getCookies());
        return context;
    }
}

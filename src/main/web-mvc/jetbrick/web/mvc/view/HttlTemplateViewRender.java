package jetbrick.web.mvc.view;

import httl.Engine;
import httl.Template;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.ViewRender;
import jetbrick.web.mvc.config.WebappConfig;
import org.apache.commons.lang.StringUtils;

public class HttlTemplateViewRender implements ViewRender {
    private final Engine engine;
    private String viewPattern = "*.httl";

    public HttlTemplateViewRender() {
        this(new Properties());
    }

    public HttlTemplateViewRender(Properties config) {
        Properties properties = getDefaultProperties();
        properties.putAll(config);

        engine = Engine.getEngine(properties);
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
        config.setProperty("remove.directive.blank.line", "false");

        config.setProperty("output.stream", "true");
        config.setProperty("output.writer", "false");

        config.setProperty("source.in.class", "false");
        config.setProperty("text.in.class", "false");
        return config;
    }

    public void setViewPattern(String viewPattern) {
        this.viewPattern = viewPattern;
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        try {
            Template template = engine.getTemplate(view);
            if (template == null) {
                rc.getResponse().sendError(404, "template not found: " + view);
                return;
            }

            HttpServletRequest request = rc.getRequest();

            Map<String, Object> context = rc.getAttributes();
            context.put("request", request);
            context.put("response", rc.getResponse());
            context.put("session", rc.getSession());
            context.put("application", rc.getServletContext());
            context.put("parameters", request.getParameterMap());
            context.put("cookies", request.getCookies());

            ServletOutputStream out = rc.getResponse().getOutputStream();
            template.render(rc.getAttributes(), out);
            out.flush();

        } catch (Exception e) {
            throw SystemException.unchecked(e);
        }
    }

    @Override
    public String getDefaultViewName(String viewName) {
        viewName = StringUtils.replace(viewPattern, "*", viewName);
        return viewName;
    }
}

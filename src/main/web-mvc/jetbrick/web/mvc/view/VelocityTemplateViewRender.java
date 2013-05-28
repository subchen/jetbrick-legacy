package jetbrick.web.mvc.view;

import java.io.*;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.ViewRender;
import jetbrick.web.mvc.config.WebappConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

public class VelocityTemplateViewRender implements ViewRender {
    private final VelocityEngine ve;
    private final String encoding;
    private String viewPattern = "*.vm";

    public VelocityTemplateViewRender() {
        this(new Properties());
    }

    public VelocityTemplateViewRender(Properties config) {
        config.putAll(getDefaultProperties());
        ve = new VelocityEngine(config);

        encoding = WebappConfig.getInstance().getEncoding();
    }

    private Properties getDefaultProperties() {
        WebappConfig webappConfig = WebappConfig.getInstance();
        File webroot = webappConfig.getWebappPath();

        Properties config = new Properties();
        config.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, webroot.getPath());
        config.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, webappConfig.isDevelopmentMode() ? "false" : "true");
        config.setProperty(Velocity.INPUT_ENCODING, webappConfig.getEncoding());
        config.setProperty(Velocity.INPUT_ENCODING, webappConfig.getEncoding());
        return config;
    }

    public void setViewPattern(String viewPattern) {
        this.viewPattern = viewPattern;
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        try {
            Template template = ve.getTemplate(view);
            if (template == null) {
                rc.getResponse().sendError(404, "template not found: " + view);
                return;
            }

            HttpServletRequest request = rc.getRequest();

            Context context = new VelocityContext(rc.getAttributes());
            context.put("request", request);
            context.put("response", rc.getResponse());
            context.put("session", rc.getSession());
            context.put("application", rc.getServletContext());
            context.put("parameters", request.getParameterMap());
            context.put("cookies", request.getCookies());

            Writer writer = new OutputStreamWriter(rc.getResponse().getOutputStream(), encoding);
            template.merge(context, writer);
            writer.flush();

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

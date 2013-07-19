package jetbrick.web.mvc.view;

import java.io.*;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.config.WebappConfig;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

public class VelocityTemplateViewRender extends AbstractViewRender {
    private final VelocityEngine ve;

    public VelocityTemplateViewRender() {
        this(new Properties());
    }

    public VelocityTemplateViewRender(Properties config) {
        config.putAll(getDefaultProperties());
        ve = new VelocityEngine(config);

        viewPattern = "*.vm";
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

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        VelocityContext context = getContext(rc);

        Writer out = rc.getResponse().getWriter();
        String layoutView = (String) context.get(layoutKey);
        if (layoutView == null) {
            // no layout 
            getTemplate(view).merge(context, out);
        } else {
            // output main content 
            StringWriter os = new StringWriter(4096);
            getTemplate(view).merge(context, os);
            context.put(mainContentKey, os);

            // output layout 
            getTemplate(layoutView).merge(context, out);
        }
        out.flush();
    }

    private Template getTemplate(String url) throws Throwable {
        Template template = ve.getTemplate(url);
        if (template == null) {
            throw new SystemException(HttpError.STATUS_404, "HTTL template not found: " + url);
        }
        return template;
    }

    private VelocityContext getContext(RequestContext rc) {
        HttpServletRequest request = rc.getRequest();
        VelocityContext context = new VelocityContext(rc.getAttributes());
        context.put("request", request);
        context.put("response", rc.getResponse());
        context.put("session", rc.getSession());
        context.put("application", rc.getServletContext());
        context.put("parameters", request.getParameterMap());
        context.put("cookies", request.getCookies());
        return context;
    }

}

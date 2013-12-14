package jetbrick.web.mvc.view;

import java.io.OutputStream;
import java.util.Map;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import jetbrick.template.ResourceNotFoundException;
import jetbrick.template.web.JetWebEngineLoader;
import jetbrick.web.mvc.RequestContext;

public class JetTemplateViewRender extends AbstractViewRender {
    public JetTemplateViewRender() {
        viewPattern = "*.jetx";
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        if (JetWebEngineLoader.unavailable()) {
            JetWebEngineLoader.setServletContext(rc.getServletContext());
        }

        Map<String, Object> context = rc.getAttributes();
        OutputStream out = rc.getResponse().getOutputStream();
        try {
            JetWebEngineLoader.getJetEngine().getTemplate(view).render(context, out);
        } catch (ResourceNotFoundException e) {
            throw new SystemException(HttpError.STATUS_404, "template not found: " + view);
        }
        out.flush();
    }
}

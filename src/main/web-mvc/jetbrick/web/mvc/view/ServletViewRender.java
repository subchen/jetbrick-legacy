package jetbrick.web.mvc.view;

import java.io.File;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.utils.CachedHttpResponse;

public class ServletViewRender extends AbstractViewRender {

    public ServletViewRender() {
        viewPattern = "*.jsp";
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        HttpServletRequest request = rc.getRequest();

        for (Map.Entry<String, Object> attr : rc.getAttributes().entrySet()) {
            request.setAttribute(attr.getKey(), attr.getValue());
        }

        String layoutView = (String) request.getAttribute(layoutKey);
        if (layoutView == null) {
            // no layout 
            doRender(rc, view);
        } else {
            // output main content 
            CachedHttpResponse response = new CachedHttpResponse(rc.getResponse(), encoding);
            request.getRequestDispatcher(view).include(request, response);
            String mainContent = response.getContent();
            request.setAttribute(mainContentKey, mainContent);

            // output layout 
            doRender(rc, layoutView);
        }
    }

    protected void doRender(RequestContext rc, String view) throws Throwable {
        if (view.endsWith(".jsp")) {
            String file = rc.getServletContext().getRealPath(view);
            if (!new File(file).exists()) {
                throw new SystemException(HttpError.STATUS_404, "jsp not found: " + view);
            }
        }
        rc.getRequest().getRequestDispatcher(view).forward(rc.getRequest(), rc.getResponse());
    }
}

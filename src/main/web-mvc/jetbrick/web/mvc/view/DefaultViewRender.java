package jetbrick.web.mvc.view;

import java.io.File;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.ViewRender;
import org.apache.commons.lang.StringUtils;

public class DefaultViewRender implements ViewRender {

    private String viewPattern = "*.jsp";

    public void setViewPattern(String viewPattern) {
        this.viewPattern = viewPattern;
    }

    @Override
    public void render(RequestContext rc, String view) throws Throwable {
        HttpServletRequest request = rc.getRequest();
        HttpServletResponse response = rc.getResponse();

        for (Map.Entry<String, Object> attr : rc.getAttributes().entrySet()) {
            request.setAttribute(attr.getKey(), attr.getValue());
        }

        if (view.endsWith(".jsp")) {
            String file = rc.getServletContext().getRealPath(view);
            if (!new File(file).exists()) {
                throw new SystemException(HttpError.STATUS_404, "jsp not found: " + view);
            }
        }

        RequestDispatcher rd = request.getRequestDispatcher(view);
        rd.forward(request, response);
    }

    @Override
    public String getDefaultViewName(String viewName) {
        viewName = StringUtils.replace(viewPattern, "*", viewName);
        return viewName;
    }
}

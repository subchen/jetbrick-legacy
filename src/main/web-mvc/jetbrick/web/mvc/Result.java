package jetbrick.web.mvc;

import java.io.*;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import jetbrick.commons.exception.HttpError;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public class Result {
    public static final Result NOOP = new Result(null, Void.class);
    public static final Result DEFAULT_VIEW = new Result(null, String.class);
    private static final String PREFIX_REDIRECT = "redirect:";

    private Object result;
    private Class<?> resultClass;

    private Result(Object result, Class<?> resultClass) {
        this.result = result;
        this.resultClass = resultClass;
    }

    public static Result of(Object result) {
        return (result == null) ? DEFAULT_VIEW : new Result(result, result.getClass());
    }

    public static Result of(Object result, Class<?> resultClass) {
        if (result == null && String.class.equals(resultClass)) {
            return DEFAULT_VIEW;
        }
        return new Result(result, resultClass);
    }

    protected void render(RequestContext rc, ViewRender render) throws Throwable {
        if (String.class.equals(resultClass)) {
            doViewRender(rc, render);
        } else if (JSONAware.class.isAssignableFrom(resultClass)) {
            doJsonRender(rc);
        } else if (InputStream.class.isAssignableFrom(resultClass)) {
            doFileInputStreamRender(rc);
        } else if (File.class.isAssignableFrom(resultClass)) {
            doFileInputStreamRender(rc);
        } else if (result instanceof Void) {
            ;
        } else {
            throw new RuntimeException("unsupport result type: " + resultClass);
        }
    }

    private void doViewRender(RequestContext rc, ViewRender viewRender) throws Throwable {
        String view = (String) result;

        if (StringUtils.isEmpty(view)) {
            String viewName = rc.getRouteInfo().getViewName();
            if (viewName == null) {
                viewName = rc.getUri();
            }
            view = viewRender.getDefaultViewName(viewName);
        }

        if (view.startsWith(PREFIX_REDIRECT)) {
            view = StringUtils.removeStart(view, PREFIX_REDIRECT);

            if (StringUtils.startsWith(view, "/")) {
                view = rc.getContextUrl(view);
            }
            rc.getResponse().sendRedirect(view);

        } else {
            viewRender.render(rc, view);
        }
    }

    private void doJsonRender(RequestContext rc) throws IOException {
        JSONAware json = (JSONAware) result;

        if (json instanceof JSONObject) {
            for (Map.Entry<String, Object> entry : rc.getAttributes().entrySet()) {
                ((JSONObject) json).put(entry.getKey(), entry.getValue());
            }
        }

        String characterEncoding = rc.getRequest().getCharacterEncoding();
        HttpServletResponse response = rc.getResponse();
        response.setContentType("application/json;charset=" + characterEncoding);
        response.setCharacterEncoding(characterEncoding);
        response.getWriter().write(json.toJSONString());
        response.getWriter().flush();
    }

    @SuppressWarnings("resource")
    private void doFileInputStreamRender(RequestContext rc) throws IOException {
        InputStream is;
        if (File.class.isAssignableFrom(resultClass)) {
            if (!((File) result).exists()) {
                throw new SystemException(HttpError.STATUS_404, "file not found: " + result);
            }
            is = new FileInputStream((File) result);
        } else {
            is = (InputStream) result;
        }

        ServletOutputStream os = rc.getResponse().getOutputStream();
        try {
            IOUtils.copy(is, os);
            os.flush();
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

}

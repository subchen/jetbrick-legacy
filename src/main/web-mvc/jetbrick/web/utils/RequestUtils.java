package jetbrick.web.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import jetbrick.commons.bean.conv.MapConverter;
import org.apache.commons.lang3.StringUtils;

public class RequestUtils extends MapConverter {

    protected HttpServletRequest request;

    public RequestUtils(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    protected Object value(String key) {
        return request.getParameter(key);
    }

    @Override
    protected String[] values(String key) {
        String[] values = request.getParameterValues(key);
        if (values == null) return null;

        if (values.length == 0) {
            values = StringUtils.split(values[0], ",");
        }
        return values;
    }

    @Override
    public boolean exist(String key) {
        return request.getParameterMap().containsKey(key);
    }

    public Object getAttribute(String key) {
        return request.getAttribute(key);
    }

    public void setAttribute(String key, Object value) {
        request.setAttribute(key, value);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    public String getClientIPAddress() {
        return ServletUtils.getClientIPAddress(request);
    }

    public String dump() {
        return ServletUtils.dump(request);
    }

}

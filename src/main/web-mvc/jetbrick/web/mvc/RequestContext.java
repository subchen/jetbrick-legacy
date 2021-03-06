package jetbrick.web.mvc;

import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.http.*;
import jetbrick.web.mvc.config.WebappConfig;
import jetbrick.web.mvc.multipart.FileItem;
import jetbrick.web.mvc.multipart.FileUploaderRequest;
import jetbrick.web.utils.RequestIntrospectUtils;

public class RequestContext {
    private final static ThreadLocal<RequestContext> threadlocalContext = new ThreadLocal<RequestContext>();
    private final WebappConfig config;
    private final HttpServletResponse response;
    private final HttpServletRequest request;
    private final RouteInfo routeInfo;
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    protected RequestContext(WebappConfig config, HttpServletRequest request, HttpServletResponse response, RouteInfo routeInfo) {
        this.config = config;
        this.request = request;
        this.response = response;
        this.routeInfo = routeInfo;

        threadlocalContext.set(this);
    }

    protected void destory() {
        attributes.clear();
        threadlocalContext.remove();
    }

    //--- thread context -------------------------------------------------
    public static RequestContext getCurrent() {
        return threadlocalContext.get();
    }

    //----- servlet ------------------------------------------
    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return request.getServletContext();
    }

    public HttpSession getSession() {
        return request.getSession();
    }

    public WebappConfig getWebappConfig() {
        return config;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }

    //---- parameters ------------------------------------------------
    public <T> T getInputs(T form) {
        RequestIntrospectUtils.introspect(form, request);
        return form;
    }

    public String getParameter(String key) {
        return request.getParameter(key);
    }

    public Integer getParameterAsInt(String key) {
        String value = request.getParameter(key);
        return value == null ? null : Integer.valueOf(value);
    }

    public Long getParameterAsLong(String key) {
        String value = request.getParameter(key);
        return value == null ? null : Long.valueOf(value);
    }

    public String[] getParameterValues(String key) {
        return request.getParameterValues(key);
    }

    public String getHeader(String key) {
        return request.getHeader(key);
    }

    public List<String> getHeaders(String key) {
        return Collections.list(request.getHeaders(key));
    }

    public Integer getId() {
        return getParameterAsInt("id");
    }

    public Integer getParentId() {
        return getParameterAsInt("parentId");
    }

    public String getRestfulParameter(int index) {
        String[] parameters = routeInfo.getRestfulParameters();
        if (index < 0 || index >= parameters.length) {
            return null;
        }
        return parameters[index];
    }

    public Integer getRestfulParameterAsInt(int index) {
        String value = getRestfulParameter(index);
        return value == null ? null : Integer.valueOf(value);
    }

    public Long getRestfulParameterAsLong(int index) {
        String value = getRestfulParameter(index);
        return value == null ? null : Long.valueOf(value);
    }

    public FileItem getFile(String name) {
        if (request instanceof FileUploaderRequest) {
            return ((FileUploaderRequest) request).getFile(name);
        }
        return null;
    }

    public Collection<FileItem> getFiles() {
        if (request instanceof FileUploaderRequest) {
            return ((FileUploaderRequest) request).getFiles().values();
        }
        return Collections.<FileItem> emptyList();
    }

    public FileItem getUniqueFile() {
        Collection<FileItem> files = getFiles();
        if (files.size() > 0) {
            return files.iterator().next();
        }
        return null;
    }

    //---- attributes ------------------------------------------------
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    //---- url ------------------------------------------------
    public String getUri() {
        String uri = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 0) {
            uri = uri + pathInfo;
        }
        return uri;
    }

    public String getContextUrl(String url) {
        return request.getContextPath() + url;
    }

    public String getFullUrl(String url) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme() + "://");
        sb.append(request.getServerName());
        sb.append(request.getServerPort() != 80 ? ":" + request.getServerPort() : "");
        sb.append(getContextUrl(url));
        return sb.toString();
    }
}

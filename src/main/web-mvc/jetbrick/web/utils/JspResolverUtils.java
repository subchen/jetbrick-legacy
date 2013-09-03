package jetbrick.web.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 从 JSP pageContext 或者 Request 中查找一个对象.
 * <ol>查找顺序:
 * <li>pageContext.getAttribute</li>
 * <li>request.getAttribute</li>
 * <li>request.getParameter</li>
 * <li>session.getAttribute</li>
 * <li>serlvetContext.getAttribute</li>
 * </ol>
 */
public class JspResolverUtils {
    protected final HttpServletRequest request;
    protected final PageContext pageContext;

    public JspResolverUtils(HttpServletRequest request) {
        this.pageContext = null;
        this.request = request;
    }

    public JspResolverUtils(PageContext pageContext) {
        this.pageContext = pageContext;
        this.request = ((HttpServletRequest) pageContext.getRequest());
    }

    public Object resolveValue(String name) {
        return resolveValue(name, request, pageContext);
    }

    public static Object resolveValue(String name, HttpServletRequest request) {
        return resolveValue(name, request, null);
    }

    public static Object resolveValue(String name, PageContext pageContext) {
        return resolveValue(name, null, pageContext);
    }

    private static Object resolveValue(String name, HttpServletRequest request, PageContext pageContext) {
        String context = name;
        context = StringUtils.substringBefore(context, ".");
        context = StringUtils.substringBefore(context, "]");

        name = StringUtils.substring(name, context.length() + 1);

        Object value = null;
        if (pageContext != null) {
            value = pageContext.getAttribute(context);
            if (value != null) {
                return getProperty(value, name);
            }
            request = (HttpServletRequest) pageContext.getRequest();
        }

        if (request != null) {
            value = request.getAttribute(context);
            if (value != null) {
                return getProperty(value, name);
            }
            value = request.getParameter(name);
            if (value != null) {
                return getProperty(value, name);
            }
            value = request.getSession().getAttribute(name);
            if (value != null) {
                return getProperty(value, name);
            }
            value = request.getSession().getServletContext().getAttribute(name);
            if (value != null) {
                return getProperty(value, name);
            }
        }
        return null;
    }

    private static Object getProperty(Object bean, String name) {
        if (StringUtils.isEmpty(name)) {
            return bean;
        } else {
            try {
                return BeanUtils.getProperty(bean, name);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
    }
}

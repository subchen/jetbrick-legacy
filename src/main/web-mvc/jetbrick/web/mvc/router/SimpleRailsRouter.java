package jetbrick.web.mvc.router;

import javax.servlet.http.HttpServletRequest;
import jetbrick.web.mvc.RouteInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 基于扩展名的 URL 路由 (*.do)
 */
public class SimpleRailsRouter extends SimpleRouter {

    public SimpleRailsRouter() {
        classNameCamelCaseEnabled = true;
        methodNameCamelCaseEnabled = false;
    }

    @Override
    public boolean accept(HttpServletRequest request) {
        return true;
    }

    @Override
    protected RouteInfo lookupRouteInfo(HttpServletRequest request, String uri) {
        uri = StringUtils.substringBeforeLast(uri, ".");
        String parts[] = uri.split("/", -1);

        String packageName = StringUtils.join(parts, ".", 0, parts.length - 1);
        String className = parts[parts.length - 1];
        String methodName = request.getParameter("m");
        if (StringUtils.isEmpty(methodName)) {
            methodName = "index";
        }

        RouteInfoImpl info = lookupControllerClass(packageName, className, methodName);
        if (info != null) {
            info.setRestfulParameters(ArrayUtils.EMPTY_STRING_ARRAY);
            info.setViewName(uri);
            return info;
        }
        return info;
    }
}

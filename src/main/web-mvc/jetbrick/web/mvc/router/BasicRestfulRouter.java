package jetbrick.web.mvc.router;

import javax.servlet.http.HttpServletRequest;
import jetbrick.web.mvc.RouteInfo;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/** 
 * RESTful URL 路由 (/users/101) 
 */
public class BasicRestfulRouter extends SimpleRouter {

    public BasicRestfulRouter() {
        classNameCamelCaseEnabled = true;
        methodNameCamelCaseEnabled = true;
    }

    @Override
    public boolean accept(HttpServletRequest request) {
        // 忽略所有带扩展名的URL 
        String uri = request.getServletPath();
        return uri.indexOf(".") < 0;
    }

    /**  
    * <ol>  
    * <li>pkg/class/...</li>  
    * <li>pkg/class/method/...</li>  
    * </ol>  
    */
    @Override
    protected RouteInfo lookupRouteInfo(HttpServletRequest request, String uri) {
        String[] parts = uri.split("/");

        for (int i = 0; i < parts.length; i++) {
            String packageName = StringUtils.join(parts, ".", 0, i);
            String className = parts[i];

            int parameterIndex = 0;
            String methodName = null;
            RouteInfoImpl info = null;
            if (i + 1 < parts.length) {
                parameterIndex = i + 2;
                methodName = parts[i + 1];
                info = lookupControllerClass(packageName, className, methodName);
            }
            if (info == null && !"index".equals(methodName)) {
                parameterIndex = i + 1;
                info = lookupControllerClass(packageName, className, "index");
            }
            if (info != null) {
                String[] parameters = (String[]) ArrayUtils.subarray(parts, parameterIndex, parts.length);
                info.setRestfulParameters(parameters);

                String viewName = StringUtils.join(parts, "/", 0, i + 1);
                info.setViewName(viewName);

                return info;
            }
        }
        return null;
    }
}

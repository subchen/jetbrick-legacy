package jetbrick.web.mvc.router;

import javax.servlet.http.HttpServletRequest;
import jetbrick.web.mvc.RouteInfo;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * RESTful URL 路由 (/users/101)
 */
public class SimpleRestfulRouter extends SimpleRouter {

    public SimpleRestfulRouter() {
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
    * <li>pkg/class</li> 
    * <li>pkg/class/method</li> 
    * <li>pkg/class/arg1-arg2-arg3</li> 
    * <li>pkg/class/method/arg1-arg2-arg3</li> 
    * </ol> 
    */
    @Override
    protected RouteInfo lookupRouteInfo(HttpServletRequest request, String uri) {
        if (uri.endsWith("/")) {
            uri = uri + "index";
        }

        String[] parts = uri.split("/");
        int partNumber = parts.length;

        //@formatter:off 
        int[][] lookupIndexes = { 
            // { class, method, args }
    		{ partNumber - 1, -1, -1 }, // pkg/class 
    		{ partNumber - 2, partNumber - 1, -1 }, // pkg/class/method 
    		{ partNumber - 2, -1, partNumber - 1 }, // pkg/class/arg1-arg2-arg3 
    		{ partNumber - 3, partNumber - 2, partNumber - 1 } // pkg/class/method/arg1-arg2-arg3 
        }; //@formatter:on 

        for (int[] index : lookupIndexes) {
            if (index[0] <= 0) return null;

            String packageName = StringUtils.join(parts, ".", 1, index[0]);
            String className = parts[index[0]];
            String methodName = index[1] > 0 ? parts[index[1]] : "index";

            RouteInfoImpl info = lookupControllerClass(packageName, className, methodName);
            if (info != null) {
                String[] parameters = ArrayUtils.EMPTY_STRING_ARRAY;
                if (index[2] > 0) {
                    parameters = parts[index[2]].split("-");
                }
                info.setRestfulParameters(parameters);

                String viewName = StringUtils.join(parts, "/", 0, index[0] + 1);
                info.setViewName(viewName);

                return info;
            }
        }
        return null;
    }
}

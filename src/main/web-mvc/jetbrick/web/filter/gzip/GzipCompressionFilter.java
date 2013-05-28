package jetbrick.web.filter.gzip;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <pre><xmp> in web.xml
 * <filter>
 *    <filter-name>gzip</filter-name>
 *    <filter-class>jetbrick.web.filter.gzip.GzipCompressionFilter</filter-class>
 * </filter>
 * 	
 * <filter-mapping>
 *   <filter-name>gzip</filter-name>
 *   <url-pattern>*.js</url-pattern>
 * </filter-mapping>
 * </xmp></pre>
 */
public class GzipCompressionFilter implements Filter {
    private static final String HAS_RUN_KEY = GzipCompressionFilter.class.getName() + ".HAS_RUN";

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean compress = false;
        if (request.getAttribute(HAS_RUN_KEY) == null && (request instanceof HttpServletRequest)) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            for (Enumeration<String> headers = httpRequest.getHeaders("Accept-Encoding"); headers.hasMoreElements();) {
                String value = headers.nextElement();
                if (value.indexOf("gzip") != -1) compress = true;
            }
        }

        request.setAttribute(HAS_RUN_KEY, "true");
        if (compress) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.addHeader("Content-Encoding", "gzip");
            GzipCompressionResponse gzipCompressionResponse = new GzipCompressionResponse(httpResponse);
            chain.doFilter(request, gzipCompressionResponse);
            gzipCompressionResponse.finish();
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

}

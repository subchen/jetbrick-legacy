package jetbrick.web.utils;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.http.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.commons.lang.EncodeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONObject;

/**
 * @since 2007-11-28
 */
public abstract class ServletUtils {

    // get webapps path in file system.
    public static File getWebappsPath() {
        // "file:/D:/shu_jwc/apache-tomcat-6.0.32/webapps/ROOT/WEB-INF/lib/sample.jar!/sample/Test.class"
        // "/D:/shu_jwc/apache-tomcat-6.0.32/webapps/ROOT/WEB-INF/classes/sample/Test.class"
        String classFilePath = "/" + StringUtils.replace(ServletUtils.class.getName(), ".", "/") + ".class";
        String classFileFullPath = ServletUtils.class.getResource(classFilePath).getFile();
        String webapps = StringUtils.substringBefore(classFileFullPath, "/WEB-INF/");
        if (webapps.startsWith("file:")) {
            webapps = StringUtils.substringAfter(webapps, "file:");
        }
        try {
            return new File(webapps).getCanonicalFile();
        } catch (IOException e) {
            throw SystemException.unchecked(e);
        }
    }

    public static JSONObject getRequestJSON(HttpServletRequest request) {
        JSONObject json = new JSONObject();
        Enumeration<String> enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String name = (String) enu.nextElement();
            String[] value = request.getParameterValues(name);
            if (value == null || value.length == 0) {
                continue;
            }
            if (value.length > 1) {
                json.put(name, value);
            } else {
                json.put(name, value[0]);
            }
        }
        return json;
    }

    /**
     * @since 2008-01-10
     */
    public static String getUrlParameters(HttpServletRequest request, String excludeNames) {
        StringBuffer sb = new StringBuffer();
        String[] excludeNamesArray = StringUtils.split(excludeNames, ",");

        Enumeration<String> enu = request.getParameterNames();
        while (enu.hasMoreElements()) {
            String name = (String) enu.nextElement();
            if (ArrayUtils.contains(excludeNamesArray, name)) continue;

            String[] value = request.getParameterValues(name);
            if (value == null) continue;
            for (int i = 0; i < value.length; i++) {
                try {
                    if (sb.length() > 0) sb.append("&");
                    sb.append(name + "=" + URLEncoder.encode(value[i], request.getCharacterEncoding()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (sb.length() > 0) sb.insert(0, "?");
        sb.insert(0, request.getContextPath() + request.getServletPath());

        return sb.toString();
    }

    public static String getClientIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-real-ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static void setBufferOff(HttpServletResponse response) {
        // Http 1.0 header
        response.setHeader("Buffer", "false");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 1L);
        // Http 1.1 header
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    public static void setFileDownloadHeader(HttpServletResponse response, String fileName, String contentType) {
        if (contentType == null) contentType = "application/x-download";
        response.setContentType(contentType);

        // 中文文件名支持
        try {
            String encodedfileName = new String(fileName.getBytes(), "ISO8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedfileName);
        } catch (UnsupportedEncodingException e) {
        }
    }

    // 是否是Ajax请求数据
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    // 是否是Pjax请求数据: https://github.com/defunkt/jquery-pjax
    public static boolean isPjaxRequest(HttpServletRequest request) {
        return StringUtils.isNotEmpty(request.getHeader("X-PJAX"));
    }

    // 是否是Flash请求数据
    public static boolean isFlashRequest(HttpServletRequest request) {
        return "Shockwave Flash".equals(request.getHeader("User-Agent")) || StringUtils.isNotEmpty(request.getHeader("x-flash-version"));
    }

    // 是否是文件上传
    public static boolean isMultipartRequest(HttpServletRequest request) {
        String type = request.getHeader("Content-Type");
        return (type != null) && (type.startsWith("multipart/form-data"));
    }

    public static boolean isGzipSupported(HttpServletRequest request) {
        String browserEncodings = request.getHeader("Accept-Encoding");
        return (browserEncodings != null) && (browserEncodings.contains("gzip"));
    }

    // 判断是否为搜索引擎
    public static boolean isRobot(HttpServletRequest request) {
        String ua = request.getHeader("user-agent");
        if (StringUtils.isBlank(ua)) return false;
        //@formatter:off
	    return (ua != null
               && (ua.indexOf("Baiduspider") != -1 
                || ua.indexOf("Googlebot") != -1
                || ua.indexOf("sogou") != -1
                || ua.indexOf("sina") != -1
                || ua.indexOf("iaskspider") != -1
                || ua.indexOf("ia_archiver") != -1
                || ua.indexOf("Sosospider") != -1
                || ua.indexOf("YoudaoBot") != -1
                || ua.indexOf("yahoo") != -1
                || ua.indexOf("yodao") != -1
                || ua.indexOf("MSNBot") != -1
                || ua.indexOf("spider") != -1
                || ua.indexOf("Twiceler") != -1
                || ua.indexOf("Sosoimagespider") != -1
                || ua.indexOf("naver.com/robots") != -1
                || ua.indexOf("Nutch") != -1
                || ua.indexOf("spider") != -1));   
	    }
	    //@formatter:on

    /**
     * 客户端对Http Basic验证的 Header进行编码.
     */
    public static String encodeHttpBasic(String userName, String password) {
        String encode = userName + ":" + password;
        return "Basic " + EncodeUtils.encodeBase64(encode.getBytes());
    }

    public static String getAuthUsername(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        String encoded = header.substring(header.indexOf(' ') + 1);
        String decoded = new String(EncodeUtils.decodeBase64(encoded));
        return decoded.substring(0, decoded.indexOf(':'));
    }

    public static String getAuthPassword(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        String encoded = header.substring(header.indexOf(' ') + 1);
        String decoded = new String(EncodeUtils.decodeBase64(encoded));
        return decoded.substring(decoded.indexOf(':') + 1);
    }

    public static String dump(HttpServletRequest request) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

        final String FORMAT = "%20s: %s\n";
        final char PADDING_CHAR = '=';
        final int PADDING_SIZE = 60;

        out.println(StringUtils.center(" Request Basic ", PADDING_SIZE, PADDING_CHAR));
        out.printf(FORMAT, "Request Date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(new Date()));
        out.printf(FORMAT, "Request URL", request.getRequestURL());
        out.printf(FORMAT, "QueryString", request.getQueryString());
        out.printf(FORMAT, "Method", request.getMethod());
        out.println();

        out.printf(FORMAT, "CharacterEncoding", request.getCharacterEncoding());
        out.printf(FORMAT, "ContentType", request.getContentType());
        out.printf(FORMAT, "ContentLength", request.getContentLength());
        out.printf(FORMAT, "Locale", request.getLocale());
        out.printf(FORMAT, "RemoteAddr", request.getRemoteAddr());
        out.println();

        out.println(StringUtils.center(" Request Headers ", PADDING_SIZE, PADDING_CHAR));
        Enumeration<String> header = request.getHeaderNames();
        while (header.hasMoreElements()) {
            String name = header.nextElement();
            String value = request.getHeader(name);
            out.printf(FORMAT, name, value);
        }
        out.println();

        out.println(StringUtils.center(" Request Parameters ", PADDING_SIZE, PADDING_CHAR));
        Enumeration<String> param = request.getParameterNames();
        while (param.hasMoreElements()) {
            String name = param.nextElement();
            String value[] = request.getParameterValues(name);
            out.printf(FORMAT, name, StringUtils.join(value, ", "));
        }
        out.println();

        out.println(StringUtils.center(" Request Cookies ", PADDING_SIZE, PADDING_CHAR));
        for (Cookie cookie : request.getCookies()) {
            out.printf(FORMAT, cookie.getName(), cookie.getValue());
        }
        out.println();

        out.println(StringUtils.repeat(PADDING_CHAR, PADDING_SIZE));
        out.flush();

        return sw.toString();
    }
}

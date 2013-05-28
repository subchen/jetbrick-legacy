package jetbrick.web.utils;

import java.io.InputStream;
import java.util.*;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 根据文件扩展名，得到MimeType类型，默认为application/octet-stream
 */
public class MimeTypeUtils {
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private static final Map<String, String> mime_type_map = initialize_map();

    public static String get(String extension) {
        if (extension == null) return null;

        String mime = mime_type_map.get(extension.toLowerCase());
        return (mime == null) ? DEFAULT_MIME_TYPE : mime;
    }

    private static Map<String, String> initialize_map() {
        Map<String, String> map = new HashMap<String, String>(1000);

        InputStream is = MimeTypeUtils.class.getResourceAsStream("MimeTypeUtils.properties");
        try {
            List<String> lines = IOUtils.readLines(is, "ISO-8859-1");
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) continue;
                if (line.startsWith("#")) continue;

                String mime = StringUtils.substringBefore(line, "=");
                String extensions = StringUtils.substringAfter(line, "=");

                String[] allExts = StringUtils.split(extensions, " ");
                for (String ext : allExts) {
                    map.put(ext, mime);
                }
            }
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }

        return map;
    }

    public static void main(String[] args) {
        System.out.println(MimeTypeUtils.get("HTML"));
        System.out.println(MimeTypeUtils.get("jpg"));
        System.out.println(MimeTypeUtils.get("html"));
        System.out.println(MimeTypeUtils.get("shtml"));
        System.out.println(MimeTypeUtils.get("class"));
    }
}

package jetbrick.commons.lang;

/**
 * 用通配符匹配字符串
 */
public class WildcharUtils {

    /**
     * 用通配符匹配字符串（支持*,?），不是正则表达式
     * <ul>
     * <li>wildMatch("IMG_????.jpg", "IMG_0001") = true
     * <li>wildMatch("IMG_*.jpg", "IMG_0001") = true
     * </ul>
     */
    public static boolean wildcharMatch(String pattern, String str) {
        pattern = toJavaPattern(pattern);
        return java.util.regex.Pattern.matches(pattern, str);
    }

    private static String toJavaPattern(String pattern) {
        StringBuilder sb = new StringBuilder();
        sb.append('^');
        char metachar[] = { '$', '^', '[', ']', '(', ')', '{', '}', '|', '+', '.', '\\' };
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            boolean isMeta = false;
            for (int j = 0; j < metachar.length; j++) {
                if (ch == metachar[j]) {
                    sb.append('\\').append(ch);
                    isMeta = true;
                    break;
                }
            }
            if (!isMeta) {
                if (ch == '*') {
                    sb.append("[\u0000-\uffff]*");
                } else {
                    sb.append(ch);
                }

            }
        }
        sb.append('$');
        return sb.toString();
    }
}

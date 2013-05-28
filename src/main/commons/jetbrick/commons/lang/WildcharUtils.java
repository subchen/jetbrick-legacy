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
        String result = "^";
        char metachar[] = { '$', '^', '[', ']', '(', ')', '{', '}', '|', '+', '.', '\\' };
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            boolean isMeta = false;
            for (int j = 0; j < metachar.length; j++) {
                if (ch == metachar[j]) {
                    result += "\\" + ch;
                    isMeta = true;
                    break;
                }
            }
            if (!isMeta) {
                if (ch == '*') {
                    result += "[\u0000-\uffff]*";
                } else {
                    result += ch;
                }

            }
        }
        result += "$";
        return result;
    }
}

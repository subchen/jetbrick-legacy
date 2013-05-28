package jetbrick.commons.text.chs;

/**
 * 得到汉字拼音首字母 (只支持 GB2312)
 * 
 * @see ChinesePinyin
 * @deprecated
 */
public abstract class ChineseSpell {

    /**
     * 得到 GB2312 拼音首字母(非中文字符保留)
     */
    public static String getFirstSpell(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            if (chineseCompareTo(current, '\u554A') < 0)
                result = result + current;
            else if (chineseCompareTo(current, '\u554A') >= 0 && chineseCompareTo(current, '\u5EA7') <= 0) {
                if (chineseCompareTo(current, '\u531D') >= 0)
                    result = result + "Z";
                else if (chineseCompareTo(current, '\u538B') >= 0)
                    result = result + "Y";
                else if (chineseCompareTo(current, '\u6614') >= 0)
                    result = result + "X";
                else if (chineseCompareTo(current, '\u6316') >= 0)
                    result = result + "W";
                else if (chineseCompareTo(current, '\u584C') >= 0)
                    result = result + "T";
                else if (chineseCompareTo(current, '\u6492') >= 0)
                    result = result + "S";
                else if (chineseCompareTo(current, '\u7136') >= 0)
                    result = result + "R";
                else if (chineseCompareTo(current, '\u671F') >= 0)
                    result = result + "Q";
                else if (chineseCompareTo(current, '\u556A') >= 0)
                    result = result + "P";
                else if (chineseCompareTo(current, '\u54E6') >= 0)
                    result = result + "O";
                else if (chineseCompareTo(current, '\u62FF') >= 0)
                    result = result + "N";
                else if (chineseCompareTo(current, '\u5988') >= 0)
                    result = result + "M";
                else if (chineseCompareTo(current, '\u5783') >= 0)
                    result = result + "L";
                else if (chineseCompareTo(current, '\u5580') >= 0)
                    result = result + "K";
                else if (chineseCompareTo(current, '\u51FB') > 0)
                    result = result + "J";
                else if (chineseCompareTo(current, '\u54C8') >= 0)
                    result = result + "H";
                else if (chineseCompareTo(current, '\u5676') >= 0)
                    result = result + "G";
                else if (chineseCompareTo(current, '\u53D1') >= 0)
                    result = result + "F";
                else if (chineseCompareTo(current, '\u86FE') >= 0)
                    result = result + "E";
                else if (chineseCompareTo(current, '\u642D') >= 0)
                    result = result + "D";
                else if (chineseCompareTo(current, '\u64E6') >= 0)
                    result = result + "C";
                else if (chineseCompareTo(current, '\u82AD') >= 0)
                    result = result + "B";
                else if (chineseCompareTo(current, '\u554A') >= 0) result = result + "A";
            } else {
                result = result + current;
            }
        }
        return result;
    }

    /**
     * 得到单个汉字的 ASCII
     */
    protected static int getCharCode(char ch) {
        try {
            byte b[] = String.valueOf(ch).getBytes("GBK");
            if (b.length == 1) { // 英文字符
                return b[0];
            } else if (b.length == 2) { // 中文字符
                int hightByte = 256 + b[0];
                int lowByte = 256 + b[1];
                return (256 * hightByte + lowByte) - 256 * 256;
            } else { // 错误
                return 0;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static int chineseCompareTo(char ch1, char ch2) {
        int s1_code = getCharCode(ch1);
        int s2_code = getCharCode(ch2);
        if (s1_code * s2_code < 0) return Math.min(s1_code, s2_code);

        return s1_code - s2_code;
    }

    public static void main(String[] args) {
        System.out.println(getFirstSpell("测试：中华人民共和国！啊"));
        System.out.println(getFirstSpell("王元军"));
        System.out.println(getFirstSpell("001高阳の圣思园♂abc鍀"));
    }
}

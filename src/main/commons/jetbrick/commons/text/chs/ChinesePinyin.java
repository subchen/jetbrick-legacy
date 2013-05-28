package jetbrick.commons.text.chs;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * 得到汉字的完整拼音 (支持GBK)
 * 
 * @since 2008-11-26
 */
public class ChinesePinyin {

    private static ChinesePinyin instance = new ChinesePinyin();
    private Map<String, String[]> pinyinTable = new HashMap<String, String[]>(21000);

    public static ChinesePinyin getInstance() {
        return instance;
    }

    private ChinesePinyin() {
        InputStream is = getClass().getResourceAsStream("ChinesePinyin.dat");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
            String line = null;
            while (true) {
                line = reader.readLine();
                if (line == null) break;

                String hex = StringUtils.substringBefore(line, "=");
                String pinyin = StringUtils.substringAfter(line, "=");
                if (StringUtils.isNotBlank(pinyin)) {
                    pinyinTable.put(hex, pinyin.split(","));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 得到指定的中文字符对应的拼音, 返回值带声调.
     * 
     * @return null - 不可识别的字符
     */
    public String[] getPinyinFromChar(char c) {
        String s = Integer.toHexString(c).toUpperCase();
        return (String[]) pinyinTable.get(s);
    }

    /**
     * 返回中文，删除不可识别的字符.
     */
    public String getChinese(String str) {
        if (str == null) return null;

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (getPinyinFromChar(ch) != null) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 得到字符串对应的拼音(默认小写, 不带声调), 不可识别的字符原样返回.
     */
    public String getFullPinyin(String str) {
        if (str == null) return null;

        StringBuffer sb = new StringBuffer();
        String[] item = null;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            item = getPinyinFromChar(ch);
            if (item == null) {
                sb.append(ch);
            } else {
                sb.append(item[0].substring(0, item[0].length() - 1));
            }
        }
        return sb.toString();
    }

    /**
     * 得到字符串对应的拼音首字母(默认小写), 不可识别的字符原样返回.
     */
    public String getFirstPinyin(String str) {
        if (str == null) return null;

        StringBuffer sb = new StringBuffer();
        String[] item = null;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            item = getPinyinFromChar(ch);
            if (item == null) {
                sb.append(ch);
            } else {
                sb.append(item[0].substring(0, 1));
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(ChinesePinyin.getInstance().getChinese("001高阳の圣思园♂abc鍀"));

        System.out.println(ChinesePinyin.getInstance().getFirstPinyin("测试：中华人民共和国！啊"));
        System.out.println(ChinesePinyin.getInstance().getFirstPinyin("王元军"));
        System.out.println(ChinesePinyin.getInstance().getFirstPinyin("001高阳の圣思园♂abc鍀"));

        System.out.println(ChinesePinyin.getInstance().getFullPinyin("测试：中华人民共和国！啊"));
        System.out.println(ChinesePinyin.getInstance().getFullPinyin("王元军"));
        System.out.println(ChinesePinyin.getInstance().getFullPinyin("001高阳の圣思园♂abc鍀"));
    }
}

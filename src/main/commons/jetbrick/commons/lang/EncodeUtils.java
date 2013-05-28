package jetbrick.commons.lang;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class EncodeUtils {
    public static String encodeMD5(String text) {
        return DigestUtils.md5Hex(text);
    }

    public static String encodeBase64(byte binaryData[]) {
        return Base64.encodeBase64String(binaryData);
    }

    public static byte[] decodeBase64(String base64String) {
        return Base64.decodeBase64(base64String);
    }

    public static String encodeBase62(byte[] binaryData) {
        return Base64.encodeBase64URLSafeString(binaryData);
    }

    public static String encodeHex(byte[] data) {
        return Hex.encodeHexString(data);
    }

    public static byte[] decodeHex(String hexStr) {
        try {
            return Hex.decodeHex(hexStr.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}

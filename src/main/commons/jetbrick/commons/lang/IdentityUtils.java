package jetbrick.commons.lang;

import java.security.SecureRandom;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * 封装各种生成唯一性ID算法的工具类.
 */
public class IdentityUtils {

    private static SecureRandom random = new SecureRandom();

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间有-分割.
     */
    public static String uuid36() {
        return UUID.randomUUID().toString();
    }

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
     */
    public static String uuid32() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String uuid16() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    /**
     * 使用SecureRandom随机生成Int.
     */
    public static int randomInt() {
        return Math.abs(random.nextInt());
    }

    /**
     * 使用SecureRandom随机生成Long.
     */
    public static long randomLong() {
        return Math.abs(random.nextLong());
    }

    /**
     * 基于Base62编码的SecureRandom随机生成bytes.
     */
    public static String randomBase62(int length) {
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return EncodeUtils.encodeBase62(randomBytes);
    }
}

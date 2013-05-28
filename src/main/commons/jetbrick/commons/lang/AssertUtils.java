package jetbrick.commons.lang;

import java.util.Collection;
import java.util.Map;
import jetbrick.commons.exception.SysError;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.lang3.StringUtils;

/**
 * 主要用于参数的合法性判断，该类 copy 自 Spring.jar
 */
public class AssertUtils {

    private static SystemException assertException(String message, Object[] args) {
        if (args == null || args.length == 0) {
            message = "[Assertion failed] - " + message;
        } else {
            message = "[Assertion failed] - " + String.format(message, args);
        }
        return new SystemException(message, SysError.ASSERT_ERROR);
    }

    public static void isTrue(boolean expression, String message, Object... args) {
        if (!expression) {
            throw assertException(message, args);
        }
    }

    public static void isTrue(boolean expression) {
        if (!expression) {
            throw assertException("this expression must be true", null);
        }
    }

    public static void isFalse(boolean expression, String message, Object... args) {
        if (expression) {
            throw assertException(message, args);
        }
    }

    public static void isFalse(boolean expression) {
        if (expression) {
            throw assertException("this expression must be false", null);
        }
    }

    public static void notNull(Object object, String message, Object... args) {
        if (object == null) {
            throw assertException(message, args);
        }
    }

    public static void notNull(Object object) {
        if (object == null) {
            throw assertException("this argument is required; it cannot be null", null);
        }
    }

    public static void notEmpty(String text, String message, Object... args) {
        if (StringUtils.isEmpty(text)) {
            throw assertException(message, args);
        }
    }

    public static void notEmpty(String text) {
        if (StringUtils.isEmpty(text)) {
            throw assertException("this String argument must have length; it cannot be null or empty", null);
        }
    }

    public static void notBlank(String text, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw assertException(message, args);
        }
    }

    public static void notBlank(String text) {
        if (StringUtils.isBlank(text)) {
            throw assertException("this String argument must have text; it cannot be null, empty, or spaces", null);
        }
    }

    public static void notEmpty(Object[] array, String message, Object... args) {
        if (array == null || array.length == 0) {
            throw assertException(message, args);
        }
    }

    public static void notEmpty(Object[] array) {
        if (array == null || array.length == 0) {
            throw assertException("this array must not be empty: it must contain at least 1 element", null);
        }
    }

    public static void notEmpty(Collection<?> collection, String message, Object... args) {
        if (collection == null || collection.isEmpty()) {
            throw assertException(message, args);
        }
    }

    public static void notEmpty(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            throw assertException("this collection must not be empty: it must contain at least 1 element", null);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message, Object... args) {
        if (map == null || map.isEmpty()) {
            throw assertException(message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            throw assertException("this map must not be empty; it must contain at least one entry", null);
        }
    }

    public static void inRange(double num, double min, double max) {
        if (num < min || num > max) {
            throw assertException("this num must between in %.2f and %.2f", new Object[] { min, max });
        }
    }

    public static void inRange(double num, double min, double max, String message, Object... args) {
        if (num < min || num > max) {
            throw assertException(message, args);
        }
    }

    public static void inRange(long num, long min, long max) {
        if (num < min || num > max) {
            throw assertException("this num must between in %d and %d", new Object[] { min, max });
        }
    }

    public static void inRange(long num, long min, long max, String message, Object... args) {
        if (num < min || num > max) {
            throw assertException(message, args);
        }
    }

}

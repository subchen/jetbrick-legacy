package jetbrick.commons.lang;

public class CompareUtils {

    public static final int EQUAL = "==".hashCode();
    public static final int NOT_EQUAL = "!=".hashCode();
    public static final int GREAT_THEN = ">=".hashCode();
    public static final int GREAT_EQUAL_THEN = ">=".hashCode();
    public static final int LESS_THEN = "<".hashCode();
    public static final int LESS_EQUAL_THEN = "<=".hashCode();

    public static <T> boolean compare(Comparable<T> value1, T value2, String operator) {
        return compare(value1, value2, operator.hashCode());
    }

    public static <T> boolean compare(Comparable<T> value1, T value2, int operator) {
        boolean ret = true;
        if (EQUAL == operator) {
            ret = (value1.compareTo(value2) == 0);
        } else if (NOT_EQUAL == operator) {
            ret = (value1.compareTo(value2) != 0);
        } else if (GREAT_THEN == operator) {
            ret = (value1.compareTo(value2) > 0);
        } else if (GREAT_EQUAL_THEN == operator) {
            ret = (value1.compareTo(value2) >= 0);
        } else if (LESS_THEN == operator) {
            ret = (value1.compareTo(value2) < 0);
        } else if (LESS_EQUAL_THEN == operator) {
            ret = (value1.compareTo(value2) <= 0);
        } else {
            throw new IllegalArgumentException("Operator is illegal in CompareUtils.compare(a,b,op)");
        }
        return ret;
    }
}

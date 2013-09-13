package jetbrick.dao.schema.data;

import java.util.Date;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.ConnectionCallback;

/**
 * 多表操作或者自定义SQL执行
 */
public interface SimpleDaoHelper {

    public abstract Dialect getDialect();

    // ----- table ---------------------------------------
    public abstract boolean tableExist(String tableName);

    // ----- query ---------------------------------------
    public abstract Integer queryAsInt(String sql, Object... parameters);

    public abstract Long queryAsLong(String sql, Object... parameters);

    public abstract String queryAsString(String sql, Object... parameters);

    public abstract Boolean queryAsBoolean(String sql, Object... parameters);

    public abstract Date queryAsDate(String sql, Object... parameters);

    public abstract <T> T[] queryAsArray(Class<T> arrayClass, String sql, Object... parameters);

    // ----- execute ---------------------------------------
    public abstract int execute(String sql, Object... parameters);

    public abstract void execute(ConnectionCallback callback);

}

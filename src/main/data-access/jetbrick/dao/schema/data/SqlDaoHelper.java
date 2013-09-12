package jetbrick.dao.schema.data;

import java.util.*;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.*;

public abstract class SqlDaoHelper {
    protected final JdbcHelper jdbc;
    protected final Dialect dialect;

    protected SqlDaoHelper(JdbcHelper jdbc) {
        this.jdbc = jdbc;
        this.dialect = jdbc.getDialect();
    }

    public Dialect getDialect() {
        return dialect;
    }

    // ----- transation ----------------------------------
    public JdbcTransaction transation() {
        return jdbc.transation();
    }

    // ----- query ---------------------------------------
    public Integer queryAsInt(String sql, Object... parameters) {
        return jdbc.queryAsInt(sql, parameters);
    }

    public Long queryAsLong(String sql, Object... parameters) {
        return jdbc.queryAsLong(sql, parameters);
    }

    public String queryAsString(String sql, Object... parameters) {
        return jdbc.queryAsString(sql, parameters);
    }

    public Boolean queryAsBoolean(String sql, Object... parameters) {
        return jdbc.queryAsBoolean(sql, parameters);
    }

    public Date queryAsDate(String sql, Object... parameters) {
        return jdbc.queryAsDate(sql, parameters);
    }

    public Map<String, Object> queryAsMap(String sql, Object... parameters) {
        return jdbc.queryAsMap(sql, parameters);
    }

    public <T> T[] queryAsArray(Class<T> arrayClass, String sql, Object... parameters) {
        return jdbc.queryAsArray(arrayClass, sql, parameters);
    }

    // ----- ddl ---------------------------------------
    public int execute(String sql, Object... parameters) {
        return jdbc.execute(sql, parameters);
    }

    public int[] executeBatch(String sql, List<Object[]> parameters) {
        return jdbc.executeBatch(sql, parameters);
    }

    public <T> T execute(ConnectionCallback<T> callback) {
        return jdbc.execute(callback);
    }
}

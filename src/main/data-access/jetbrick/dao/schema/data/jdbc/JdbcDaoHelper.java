package jetbrick.dao.schema.data.jdbc;

import java.util.*;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.ConnectionCallback;
import jetbrick.dao.orm.jdbc.JdbcHelper;
import jetbrick.dao.orm.jdbc.JdbcTransaction;
import jetbrick.dao.schema.data.SimpleDaoHelper;

public class JdbcDaoHelper implements SimpleDaoHelper {
    protected final JdbcHelper dao;
    protected final Dialect dialect;

    public JdbcDaoHelper(JdbcHelper dao) {
        this.dao = dao;
        this.dialect = dao.getDialect();
    }

    public JdbcTransaction transation() {
        return dao.transation();
    }

    public JdbcHelper getJdbcHelper() {
        return dao;
    }

    // ----- dialect ---------------------------------------
    @Override
    public Dialect getDialect() {
        return dialect;
    }

    // ----- table ---------------------------------------
    @Override
    public boolean tableExist(String tableName) {
        return dao.tableExist(tableName);
    }

    // ----- execute ---------------------------------------
    @Override
    public int execute(String sql, Object... parameters) {
        return dao.execute(sql, parameters);
    }

    public int[] executeBatch(String sql, List<Object[]> parameters) {
        return dao.executeBatch(sql, parameters);
    }

    @Override
    public void execute(ConnectionCallback callback) {
        dao.execute(callback);
    }

    // ----- query ---------------------------------------
    @Override
    public Integer queryAsInt(String sql, Object... parameters) {
        return dao.queryAsInt(sql, parameters);
    }

    @Override
    public Long queryAsLong(String sql, Object... parameters) {
        return dao.queryAsLong(sql, parameters);
    }

    @Override
    public String queryAsString(String sql, Object... parameters) {
        return dao.queryAsString(sql, parameters);
    }

    @Override
    public Boolean queryAsBoolean(String sql, Object... parameters) {
        return dao.queryAsBoolean(sql, parameters);
    }

    @Override
    public Date queryAsDate(String sql, Object... parameters) {
        return dao.queryAsDate(sql, parameters);
    }

    public Map<String, Object> queryAsMap(String sql, Object... parameters) {
        return dao.queryAsMap(sql, parameters);
    }

    @Override
    public <T> T[] queryAsArray(Class<T> arrayClass, String sql, Object... parameters) {
        return dao.queryAsArray(arrayClass, sql, parameters);
    }

}

package jetbrick.dao.orm;

import java.sql.*;
import java.util.*;
import java.util.Date;
import javax.sql.DataSource;
import jetbrick.commons.exception.DbError;
import jetbrick.commons.exception.SystemException;
import jetbrick.commons.lang.AssertUtils;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.handler.*;
import jetbrick.dao.orm.mapper.*;
import jetbrick.dao.orm.utils.*;
import jetbrick.dao.schema.data.Pagelist;

/**
 * 数据库操作。单例使用
 */
public class JdbcHelper {
    // 当前线程(事务)
    private final ThreadLocal<JdbcTransaction> transationHandler = new ThreadLocal<JdbcTransaction>();
    private final DataSource dataSource;
    private final Dialect dialect;

    public JdbcHelper(DataSource dataSource) {
        this.dataSource = dataSource;
        this.dialect = doGetDialet();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 启动一个事务
     */
    public JdbcTransaction transation() {
        if (transationHandler.get() != null) {
            throw new SystemException("current transaction has not been close.", DbError.TRANSACTION_ERROR);
        }
        try {
            JdbcTransaction tx = new JdbcTransaction(dataSource.getConnection(), transationHandler);
            transationHandler.set(tx);
            return tx;
        } catch (SQLException e) {
            throw SystemException.unchecked(e, DbError.TRANSACTION_ERROR);
        }
    }

    /**
     * 获取一个当前线程的连接(事务中)，如果没有，则新建一个。
     */
    private Connection getConnection() {
        JdbcTransaction tx = transationHandler.get();
        try {
            if (tx == null) {
                return dataSource.getConnection();
            } else {
                return tx.getConnection();
            }
        } catch (SQLException e) {
            throw SystemException.unchecked(e);
        }
    }

    /**
     * 释放一个连接，如果不在conn不在事务中，则关闭它，否则不处理。
     */
    private void closeConnection(Connection conn) {
        if (transationHandler.get() == null) {
            // not in transaction
            JdbcUtils.closeQuietly(conn);
        }
    }

    public <T> List<T> queryAsList(RowMapper<T> rowMapper, String sql, Object... parameters) {
        AssertUtils.notNull(rowMapper, "rowMapper is null.");

        ResultSetHandler<List<T>> rsh = new RowListHandler<T>(rowMapper);
        return query(rsh, sql, parameters);
    }

    public <T> List<T> queryAsList(Class<T> beanClass, String sql, Object... parameters) {
        AssertUtils.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = getRowMapper(beanClass);
        return queryAsList(rowMapper, sql, parameters);
    }

    public <T> T queryAsObject(RowMapper<T> rowMapper, String sql, Object... parameters) {
        ResultSetHandler<T> rsh = new SingleRowHandler<T>(rowMapper);
        return query(rsh, sql, parameters);
    }

    public <T> T queryAsObject(Class<T> beanClass, String sql, Object... parameters) {
        AssertUtils.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = getRowMapper(beanClass);
        return queryAsObject(rowMapper, sql, parameters);
    }

    public Integer queryAsInt(String sql, Object... parameters) {
        return queryAsObject(Integer.class, sql, parameters);
    }

    public Long queryAsLong(String sql, Object... parameters) {
        return queryAsObject(Long.class, sql, parameters);
    }

    public String queryAsString(String sql, Object... parameters) {
        return queryAsObject(String.class, sql, parameters);
    }

    public Boolean queryAsBoolean(String sql, Object... parameters) {
        return queryAsObject(Boolean.class, sql, parameters);
    }

    public Date queryAsDate(String sql, Object... parameters) {
        return queryAsObject(Date.class, sql, parameters);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> queryAsMap(String sql, Object... parameters) {
        return queryAsObject(Map.class, sql, parameters);
    }

    public <T> T[] queryAsArray(Class<T> arrayClass, String sql, Object... parameters) {
        try {
            Class<T[]> clazz = (Class<T[]>) Class.forName("[" + arrayClass.getName());
            return queryAsObject(clazz, sql, parameters);
        } catch (ClassNotFoundException e) {
            throw SystemException.unchecked(e);
        }
    }

    public <T> Pagelist queryAsPagelist(Pagelist pagelist, Class<T> beanClass, String sql, Object... parameters) {
        AssertUtils.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = getRowMapper(beanClass);
        return queryAsPagelist(pagelist, rowMapper, sql, parameters);
    }

    public <T> Pagelist queryAsPagelist(Pagelist pagelist, RowMapper<T> rowMapper, String sql, Object... parameters) {
        AssertUtils.notNull(pagelist, "pagelist is null.");
        AssertUtils.notNull(rowMapper, "rowMapper is null.");

        if (pagelist.getCount() < 0) {
            String count_sql = SqlUtils.sql_get_count(sql);
            int count = queryAsInt(count_sql, parameters);
            pagelist.setCount(count);
        }

        List<?> items = Collections.emptyList();
        if (pagelist.getCount() > 0) {
            String page_sql = dialect.sql_pagelist(sql, pagelist.getFirstResult(), pagelist.getPageSize());
            PagelistHandler<T> rsh = new PagelistHandler<T>(rowMapper);
            if (page_sql == null) {
                // 如果不支持分页，那么使用原始的分页方法 ResultSet.absolute(first)
                rsh.setFirstResult(pagelist.getFirstResult());
            } else {
                // 使用数据库自身的分页SQL语句，将直接返回某一个
                rsh.setFirstResult(0);
                sql = page_sql;
            }
            rsh.setMaxResults(pagelist.getPageSize());
            items = query(rsh, sql, parameters);
        }
        pagelist.setItems(items);

        return pagelist;
    }

    public <T> T query(ResultSetHandler<T> rsh, String sql, Object... parameters) {
        AssertUtils.notNull(rsh, "rsh is null.");
        AssertUtils.notNull(sql, "sql is null.");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T result = null;

        try {
            conn = getConnection();
            ps = PreparedStatementCreator.createPreparedStatement(conn, sql, parameters);
            rs = ps.executeQuery();
            result = rsh.handle(rs);
        } catch (Throwable e) {
            throw SystemException.unchecked(e).set("sql", sql).set("parameters", parameters);
        } finally {
            JdbcUtils.closeQuietly(rs);
            JdbcUtils.closeQuietly(ps);
            closeConnection(conn);
        }

        return result;
    }

    public int execute(String sql, Object... parameters) {
        AssertUtils.notNull(sql, "sql is null.");

        Connection conn = null;
        PreparedStatement ps = null;
        int rows = 0;

        try {
            conn = getConnection();
            ps = PreparedStatementCreator.createPreparedStatement(conn, sql, parameters);
            rows = ps.executeUpdate();
        } catch (SQLException e) {
            throw SystemException.unchecked(e).set("sql", sql).set("parameters", parameters);
        } finally {
            JdbcUtils.closeQuietly(ps);
            closeConnection(conn);
        }

        return rows;
    }

    public int[] executeBatch(String sql, List<Object[]> parameters) {
        AssertUtils.notNull(sql, "sql is null.");

        Connection conn = null;
        PreparedStatement ps = null;
        int[] rows;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            for (Object[] parameter : parameters) {
                for (int i = 0; i < parameter.length; i++) {
                    ps.setObject(i + 1, parameter[i]);
                }
                ps.addBatch();
            }
            rows = ps.executeBatch();
        } catch (SQLException e) {
            throw SystemException.unchecked(e).set("sql", sql).set("parameters", parameters);
        } finally {
            JdbcUtils.closeQuietly(ps);
            closeConnection(conn);
        }

        return rows;
    }

    public void execute(ConnectionCallback callback) {
        Connection conn = null;
        try {
            conn = getConnection();
            callback.execute(conn);
        } catch (SQLException e) {
            throw SystemException.unchecked(e);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * 判断表是否已经存在
     */
    public boolean tableExist(String name) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, name.toUpperCase(), new String[] { "TABLE" });
            return rs.next();
        } catch (SQLException e) {
            throw SystemException.unchecked(e);
        } finally {
            JdbcUtils.closeQuietly(rs);
            closeConnection(conn);
        }
    }

    public Dialect getDialect() {
        return dialect;
    }

    private Dialect doGetDialet() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String name = conn.getMetaData().getDatabaseProductName();
            return Dialect.getDialect(name);
        } catch (SQLException e) {
            throw SystemException.unchecked(e);
        } finally {
            JdbcUtils.closeQuietly(conn);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> RowMapper<T> getRowMapper(Class<T> beanClass) {
        RowMapper<T> rowMapper;
        if (beanClass.isArray()) {
            rowMapper = (RowMapper<T>) new ArrayRowMapper();
        } else if (beanClass.getName().equals("java.util.Map")) {
            rowMapper = (RowMapper<T>) new MapRowMapper();
        } else if (beanClass.getName().startsWith("java.")) {
            rowMapper = new SingleColumnRowMapper<T>(beanClass);
        } else {
            rowMapper = new BeanRowMapper<T>(beanClass);
        }
        return rowMapper;
    }
}

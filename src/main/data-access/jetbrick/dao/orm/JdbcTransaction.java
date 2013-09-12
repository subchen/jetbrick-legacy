package jetbrick.dao.orm;

import java.sql.Connection;
import java.sql.SQLException;
import jetbrick.commons.exception.DbError;
import jetbrick.commons.exception.SystemException;
import jetbrick.dao.orm.utils.JdbcUtils;

/**
 * 事务对象
 */
public class JdbcTransaction {
    private Connection conn;
    private ThreadLocal<JdbcTransaction> transationHandler;

    protected JdbcTransaction(Connection conn, ThreadLocal<JdbcTransaction> transationHandler) {
        this.conn = conn;
        this.transationHandler = transationHandler;

        try {
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw SystemException.unchecked(e, DbError.TRANSACTION_ERROR);
        }
    }

    public Connection getConnection() {
        return conn;
    }

    /**
     * 提交一个事务
     */
    public void commit() {
        try {
            if (conn.isClosed()) {
                throw new SystemException("the connection is closed in transaction.", DbError.TRANSACTION_ERROR);
            }
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw SystemException.unchecked(e, DbError.TRANSACTION_ERROR);
        }
    }

    /**
     * 回滚一个事务
     */
    public void rollback() {
        try {
            if (conn.isClosed()) {
                throw new SystemException("the connection is closed in transaction.", DbError.TRANSACTION_ERROR);
            }
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw SystemException.unchecked(e, DbError.TRANSACTION_ERROR);
        }
    }

    /**
     * 结束一个事务
     */
    public void close() {
        try {
            if (conn.isClosed()) {
                throw new SystemException("the connection is closed in transaction.", DbError.TRANSACTION_ERROR);
            }
            JdbcUtils.closeQuietly(conn);
        } catch (SQLException e) {
            throw SystemException.unchecked(e, DbError.TRANSACTION_ERROR);
        } finally {
            transationHandler.set(null);
        }
    }

}

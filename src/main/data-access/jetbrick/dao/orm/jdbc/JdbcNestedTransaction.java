package jetbrick.dao.orm.jdbc;

import java.sql.*;
import jetbrick.commons.exception.DbError;
import jetbrick.commons.exception.SystemException;
import jetbrick.dao.orm.Transaction;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Jdbc 子事务
 */
public class JdbcNestedTransaction implements Transaction {
    private Connection conn;
    private Savepoint savepoint;

    protected JdbcNestedTransaction(Connection conn) {
        this.conn = conn;

        try {
            savepoint = conn.setSavepoint(RandomStringUtils.randomAlphabetic(4));
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
            // 子事务不需要 commit
            // conn.commit(savepoint);
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
            conn.rollback(savepoint);
        } catch (SQLException e) {
            throw SystemException.unchecked(e, DbError.TRANSACTION_ERROR);
        }
    }

    /**
     * 结束一个事务
     */
    public void close() {
        // 子事务不需要 close
    }

}

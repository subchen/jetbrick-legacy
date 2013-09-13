package jetbrick.dao.schema.data.hibernate;

import jetbrick.commons.exception.DbError;
import jetbrick.commons.exception.SystemException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * 事务对象
 */
public class HibernateTransaction {
    private final Session session;
    private final Transaction transaction;
    private final ThreadLocal<HibernateTransaction> transationHandler;

    protected HibernateTransaction(Session session, ThreadLocal<HibernateTransaction> transationHandler) {
        this.session = session;
        this.transationHandler = transationHandler;

        this.transaction = session.beginTransaction();
    }

    public Session getSession() {
        return session;
    }

    /**
     * 提交一个事务
     */
    public void commit() {
        if (!session.isOpen()) {
            throw new SystemException("the session is closed in transaction.", DbError.TRANSACTION_ERROR);
        }
        transaction.commit();
    }

    /**
     * 回滚一个事务
     */
    public void rollback() {
        if (!session.isOpen()) {
            throw new SystemException("the connection is closed in transaction.", DbError.TRANSACTION_ERROR);
        }
        transaction.rollback();
    }

    /**
     * 结束一个事务
     */
    public void close() {
        try {
            if (!session.isOpen()) {
                throw new SystemException("the connection is closed in transaction.", DbError.TRANSACTION_ERROR);
            }
            session.close();
        } finally {
            transationHandler.set(null);
        }
    }
}

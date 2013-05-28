package jetbrick.dao.log;

import java.lang.reflect.*;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PreparedStatement Wrapper to add logging
 */
public class JdbcLogPreparedStatement extends JdbcLogSupport implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcLogPreparedStatement.class);
    private PreparedStatement statement;
    private String sql;

    private JdbcLogPreparedStatement(PreparedStatement stmt, String sql) {
        this.statement = stmt;
        this.sql = sql;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            Object value = method.invoke(statement, params);
            if (EXECUTE_METHODS.contains(method.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("#{} PreparedStatement.{}(): {}", id, method.getName(), formatSQL(sql));
                    log.debug("#{} Parameters: {}", id, getParamValueList());
                    log.debug("#{} Types: {}", id, getParamTypeList());
                }
                resetParamsInfo();
            }
            if (RESULTSET_METHODS.contains(method.getName())) {
                if (value != null && value instanceof ResultSet) {
                    value = JdbcLogResultSet.getInstance((ResultSet) value);
                }
            } else if (SET_METHODS.contains(method.getName())) {
                if ("setNull".equals(method.getName())) {
                    addParam(params[0], null);
                } else {
                    addParam(params[0], params[1]);
                }
            }
            return value;
        } catch (Throwable t) {
            throw unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a PreparedStatement
     * 
     * @param stmt
     *            - the statement
     * @param sql
     *            - the sql statement
     * @return - the proxy
     */
    public static PreparedStatement getInstance(PreparedStatement stmt, String sql) {
        if (stmt instanceof JdbcLogPreparedStatement) {
            return stmt;
        } else {
            InvocationHandler handler = new JdbcLogPreparedStatement(stmt, sql);
            ClassLoader cl = PreparedStatement.class.getClassLoader();
            return (PreparedStatement) Proxy.newProxyInstance(cl, new Class[] { PreparedStatement.class, CallableStatement.class }, handler);
        }
    }

}

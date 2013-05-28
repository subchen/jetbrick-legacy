package jetbrick.dao.log;

import java.lang.reflect.*;
import java.sql.*;

/**
 * Connection Wrapper to add logging
 */
public class JdbcLogConnection extends JdbcLogSupport implements InvocationHandler {
    private Connection connection;

    private JdbcLogConnection(Connection conn) {
        this.connection = conn;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        try {
            String methodName = method.getName();
            if ("prepareStatement".equals(methodName)) {
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = JdbcLogPreparedStatement.getInstance(stmt, (String) params[0]);
                return stmt;
            } else if ("prepareCall".equals(methodName)) {
                PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
                stmt = JdbcLogPreparedStatement.getInstance(stmt, (String) params[0]);
                return stmt;
            } else if ("createStatement".equals(methodName)) {
                Statement stmt = (Statement) method.invoke(connection, params);
                stmt = JdbcLogStatement.getInstance(stmt);
                return stmt;
            } else {
                return method.invoke(connection, params);
            }
        } catch (Throwable t) {
            throw unwrapThrowable(t);
        }
    }

    /**
     * Creates a logging version of a connection
     * 
     * @param conn - the original connection
     * @return - the connection with logging
     */
    public static Connection getInstance(Connection conn) {
        if (conn instanceof JdbcLogConnection) {
            return conn;
        } else {
            InvocationHandler handler = new JdbcLogConnection(conn);
            ClassLoader cl = Connection.class.getClassLoader();
            return (Connection) Proxy.newProxyInstance(cl, new Class[] { Connection.class }, handler);
        }
    }
}

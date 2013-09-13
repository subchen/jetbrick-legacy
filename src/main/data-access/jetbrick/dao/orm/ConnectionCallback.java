package jetbrick.dao.orm;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCallback {

    public void execute(Connection conn) throws SQLException;

}

package jetbrick.dao.hibernate;

import java.sql.Connection;

public interface JdbcTransaction {

    public void execute(Connection conn);

}

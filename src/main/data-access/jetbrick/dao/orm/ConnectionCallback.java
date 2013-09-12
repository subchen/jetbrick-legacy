package jetbrick.dao.orm;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCallback<T> {

	public T doInConnection(Connection conn) throws SQLException;
	
}
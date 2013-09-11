package jetbrick.dao.oam;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCallback<T> {

	public T doInConnection(Connection conn) throws SQLException;
	
}
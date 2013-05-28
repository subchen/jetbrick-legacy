package jetbrick.dao.oam;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCallback {

	public Object doInConnection(Connection conn) throws SQLException;
	
}
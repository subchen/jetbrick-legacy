package jetbrick.dao.oam.utils;

import java.sql.*;

public final class JdbcUtils {
	public static void closeQuietly(Connection conn) {
		try {
			if (conn != null) conn.close();
		} catch (SQLException e) {
		}
	}

	public static void closeQuietly(Statement stmt) {
		try {
			if (stmt != null) stmt.close();
		} catch (SQLException e) {
		}
	}

	public static void closeQuietly(ResultSet rs) {
		try {
			if (rs != null) rs.close();
		} catch (SQLException e) {
		}
	}

}

package jetbrick.dao.oam;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 将 一行 ResultSet 转换成 T，不允许调用 rs.next()
 */
public interface RowMapper<T> {

    public T handle(ResultSet rs) throws SQLException;

}

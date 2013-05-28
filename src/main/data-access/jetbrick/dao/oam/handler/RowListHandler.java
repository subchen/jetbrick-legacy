package jetbrick.dao.oam.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jetbrick.dao.oam.ResultSetHandler;
import jetbrick.dao.oam.RowMapper;

public class RowListHandler<T> implements ResultSetHandler<List<T>> {

    private RowMapper<T> mapper;

    public RowListHandler(RowMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<T> handle(ResultSet rs) throws SQLException {
        List<T> rows = new ArrayList<T>();
        while (rs.next()) {
            rows.add(mapper.handle(rs));
        }
        return rows;
    }
}

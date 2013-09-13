package jetbrick.dao.orm.jdbc.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import jetbrick.dao.orm.jdbc.ResultSetHandler;
import jetbrick.dao.orm.jdbc.RowMapper;

public class SingleRowHandler<T> implements ResultSetHandler<T> {

    private RowMapper<T> mapper;

    public SingleRowHandler(RowMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return mapper.handle(rs);
        }
        return null;
    }
}

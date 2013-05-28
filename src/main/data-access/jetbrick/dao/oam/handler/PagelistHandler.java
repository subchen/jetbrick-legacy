package jetbrick.dao.oam.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import jetbrick.dao.oam.ResultSetHandler;
import jetbrick.dao.oam.RowMapper;

public class PagelistHandler<T> implements ResultSetHandler<List<T>> {

	private RowMapper<T> mapper;
	private int first;
	private int max;

	public PagelistHandler(RowMapper<T> mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		if (first > 1) {
			boolean succ = rs.absolute(first - 1);
			if (!succ) {
				return Collections.emptyList();
			}
		}

		List<T> rows = new ArrayList<T>();
		while (rs.next()) {
			rows.add(mapper.handle(rs));
			if (max > 0 && rows.size() >= max) {
				break;
			}
		}
		return rows;
	}

	public void setFirstResult(int first) {
		this.first = first;
	}

	public void setMaxResults(int max) {
		this.max = max;
	}
}

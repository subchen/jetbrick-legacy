package jetbrick.dao.orm.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import jetbrick.dao.orm.jdbc.RowMapper;
import org.apache.commons.beanutils.ConvertUtils;

public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private Class<T> targetClass;

    public SingleColumnRowMapper(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T handle(ResultSet rs) throws SQLException {
        Object result = rs.getObject(1);
        return (T) ConvertUtils.convert(result, targetClass);
    }

}

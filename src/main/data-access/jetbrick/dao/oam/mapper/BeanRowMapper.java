package jetbrick.dao.oam.mapper;

import java.sql.*;
import jetbrick.commons.lang.CamelCaseUtils;
import jetbrick.dao.oam.RowMapper;
import org.apache.commons.beanutils.PropertyUtils;

public class BeanRowMapper<T> implements RowMapper<T> {

    private Class<T> beanClass;

    public BeanRowMapper(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public T handle(ResultSet rs) throws SQLException {
        try {
            T bean = beanClass.newInstance();

            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String columnName = rsmd.getColumnLabel(i);
                if (columnName == null || columnName.length() == 0) {
                    columnName = rsmd.getColumnName(i);
                }

                String propertyName = (String) CamelCaseUtils.toCamelCase(columnName);
                Object value = rs.getObject(i);

                PropertyUtils.setSimpleProperty(bean, propertyName, value);
            }
            return bean;

        } catch (Throwable e) {
            throw new SQLException("Can't set bean property.", e);
        }
    }

}

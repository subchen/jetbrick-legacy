package jetbrick.dao.utils;

import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.oam.JdbcTemplate;
import jetbrick.dao.schema.data.PersistentDAO;

/**
 * 负责全局获取数据库操作对象
 */
public class DbUtils {
    private static PersistentDAO dao;

    public static PersistentDAO dao() {
        if (dao == null) {
            synchronized (DbUtils.class) {
                if (dao == null) {
                    dao = lazy_getPersistentDAO();
                }
            }
        }
        return dao;
    }

    public static Dialect getDialect() {
        return dao().getDialect();
    }

    private static PersistentDAO lazy_getPersistentDAO() {
        JdbcTemplate jdbc = new JdbcTemplate(DataSourceUtils.getDataSource());
        return new PersistentDAO(jdbc);
    }
}

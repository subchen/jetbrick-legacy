package jetbrick.dao.schema.data.hibernate;

import javax.sql.DataSource;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.DataSourceUtils;
import jetbrick.dao.schema.data.EntityUtils;
import jetbrick.dao.schema.data.SchemaInfo;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

public class HibernateEntity {

    public static final HibernateDaoHelper DAOHelper = new HibernateDaoHelper(getSessionFactory());

    private static SessionFactory getSessionFactory() {
        DataSource dataSource = DataSourceUtils.getDataSource();
        Dialect dialect = DataSourceUtils.getDialect();

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        for (SchemaInfo<?> schema : EntityUtils.getSchemaList()) {
            String path = schema.getTableClass().getPackage().getName().replace(".", "/");
            String file = schema.getTableClass().getSimpleName() + ".hbm.xml";
            path = "classpath:" + path + "/hbm_" + dialect.getName() + "/" + file;
            builder.addResource(path);
        }

        builder.setProperty(Environment.DIALECT, dialect.getHibernateDialect());
        builder.setProperty(Environment.SHOW_SQL, "true");
        builder.setProperty(Environment.STATEMENT_BATCH_SIZE, "100");

        return builder.buildSessionFactory();
    }
}

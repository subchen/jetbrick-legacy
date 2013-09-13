package jetbrick.dao.schema.data.hibernate;

import org.hibernate.SessionFactory;

public class HibernateEntity {

    public static final HibernateDaoHelper DAOHelper = new HibernateDaoHelper(getSessionFactory());

    private static SessionFactory getSessionFactory() {
        return null;
    }

}

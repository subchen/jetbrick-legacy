package jetbrick.web.mvc.intercept.spi;

import jetbrick.dao.orm.Transaction;
import jetbrick.dao.schema.data.hibernate.HibernateEntity;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.config.WebappConfig;
import jetbrick.web.mvc.intercept.Interceptor;
import jetbrick.web.mvc.intercept.InterceptorChain;

/**
 * 针对 Hibernate 的事务支持
 */
public class HibernateTransationInterceptor implements Interceptor {

    @Override
    public void intercept(RequestContext rc, InterceptorChain chain) throws Throwable {
        Transaction tx = HibernateEntity.DAO.transaction();
        try {
            chain.invork(rc);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw e;
        } finally {
            tx.close();
        }
    }

    @Override
    public void init(WebappConfig config) {
    }

    @Override
    public void destory() {
    }

}

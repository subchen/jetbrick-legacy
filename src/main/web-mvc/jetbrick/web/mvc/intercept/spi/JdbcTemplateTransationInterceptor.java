package jetbrick.web.mvc.intercept.spi;

import jetbrick.dao.orm.JdbcTransaction;
import jetbrick.dao.schema.data.Entity;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.config.WebappConfig;
import jetbrick.web.mvc.intercept.Interceptor;
import jetbrick.web.mvc.intercept.InterceptorChain;

/**
 * 针对 JdbcHelper 的事务支持
 */
public class JdbcTemplateTransationInterceptor implements Interceptor {

    @Override
    public void intercept(RequestContext rc, InterceptorChain chain) throws Throwable {
        JdbcTransaction tx = Entity.JDBC.transation();
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

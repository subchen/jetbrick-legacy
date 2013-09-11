package jetbrick.web.mvc.intercept.spi;

import jetbrick.dao.oam.JdbcTransaction;
import jetbrick.dao.schema.data.EntityUtils;
import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.config.WebappConfig;
import jetbrick.web.mvc.intercept.Interceptor;
import jetbrick.web.mvc.intercept.InterceptorChain;

/**
 * 针对 JdbcTemplate 的事务支持
 */
public class JdbcTemplateTransationInterceptor implements Interceptor {

    @Override
    public void intercept(RequestContext rc, InterceptorChain chain) throws Throwable {
        JdbcTransaction tx = EntityUtils.JDBC.transation();
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

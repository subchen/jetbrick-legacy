package jetbrick.web.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class Hibernate3OpenSessionInViewFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(Hibernate3OpenSessionInViewFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // see OpenSessionInViewFilter.java in spring.jar

        boolean hibernateSessionParticipate = false;

        ServletContext servletContext = ((HttpServletRequest) request).getSession().getServletContext();
        ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        if (appContext == null) {
            log.error("there isn't Spring ContextLoader* definitions in your web.xml!");
            return;
        }

        // open transaction
        SessionFactory sessionFactory = (SessionFactory) appContext.getBean("sessionFactory");
        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            hibernateSessionParticipate = true;
        } else {
            log.debug("Opening single Hibernate Session in OpenSessionInViewFilter");
            Session session = SessionFactoryUtils.getSession(sessionFactory, true);
            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        }

        try {
            // do action & jsp
            chain.doFilter(request, response);

        } finally {
            // close transaction
            if (!hibernateSessionParticipate) {
                SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
                log.debug("Closing single Hibernate Session in OpenSessionInViewFilter");
                SessionFactoryUtils.closeSession(sessionHolder.getSession());
            }
        }
    }

    @Override
    public void destroy() {
    }

}

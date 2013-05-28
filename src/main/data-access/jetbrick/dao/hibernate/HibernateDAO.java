package jetbrick.dao.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import jetbrick.dao.schema.data.Pagelist;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.collections.iterators.SingletonIterator;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.classic.QueryTranslatorImpl;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateDAO {

	protected static Logger log = LoggerFactory.getLogger(HibernateDAO.class);
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	protected Session getSession() {
		try {
			return sessionFactory.getCurrentSession();
		} catch (Exception e) {
			log.warn(e.getMessage());
			return sessionFactory.openSession();
		}
	}

	public void execute(final JdbcTransaction trans) {
		getSession().doWork(new Work() {
			@Override
			public void execute(Connection conn) throws SQLException {
				trans.execute(conn);
			}
		});
	}

	public void save(Object... entities) {
		Session session = getSession();
		for (Object entity : entities) {
			if (entity instanceof Collection) {
				saveAll((Collection<?>) entity);
			} else {
				session.save(entity);
			}
		}
	}

	public void update(Object... entities) {
		Session session = getSession();
		for (Object entity : entities) {
			if (entity instanceof Collection) {
				updateAll((Collection<?>) entity);
			} else {
				session.update(entity);
			}
		}
	}

	public void saveOrUpdate(Object... entities) {
		Session session = getSession();
		for (Object entity : entities) {
			if (entity instanceof Collection) {
				saveOrUpdateAll((Collection<?>) entity);
			} else {
				session.saveOrUpdate(entity);
			}
		}
	}

	public void delete(Object... entities) {
		Session session = getSession();
		for (Object entity : entities) {
			if (entity instanceof Collection) {
				deleteAll((Collection<?>) entity);
			} else {
				session.delete(entity);
			}
		}
	}

	public void saveAll(Collection<?> entities) {
		Session session = getSession();
		for (Object entity : entities) {
			session.save(entity);
		}
	}

	public void updateAll(Collection<?> entities) {
		Session session = getSession();
		for (Object entity : entities) {
			session.update(entity);
		}
	}

	public void saveOrUpdateAll(Collection<?> entities) {
		Session session = getSession();
		for (Object entity : entities) {
			session.saveOrUpdate(entity);
		}
	}

	public void deleteAll(Collection<?> entities) {
		Session session = getSession();
		for (Object entity : entities) {
			session.delete(entity);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> clazz, Serializable id) {
		return (T) getSession().get(clazz, id);
	}

	public <T> void deleteObject(Class<T> clazz, Serializable id) {
		delete(getObject(clazz, id));
	}

	public int deleteObjects(Class<?> clazz, String name, Object value) {
		String hql = "delete from " + clazz.getName() + " where " + name + "=?";
		if (value == null) {
			hql = "delete from " + clazz.getName() + " where " + name + " is null";
		} else if (value instanceof Object[]) {
			hql = "delete from " + clazz.getName() + " where " + name + " in (:list0)";
		} else if (value instanceof Collection) {
			hql = "delete from " + clazz.getName() + " where " + name + " in (:list0)";
		}
		Iterator<?> parameters = (value == null) ? null : new SingletonIterator(value);
		return createQueryByIterator(getSession(), hql, parameters).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public <T> T findObject(Class<T> clazz, String name, Object value) {
		String hql = "from " + clazz.getName() + " where " + name + "=?";
		if (value == null) {
			hql = "from " + clazz.getName() + " where " + name + " is null";
		} else if (value instanceof Object[]) {
			hql = "from " + clazz.getName() + " where " + name + " in (:list0)";
		} else if (value instanceof Collection) {
			hql = "from " + clazz.getName() + " where " + name + " in (:list0)";
		}
		Iterator<?> parameters = (value == null) ? null : new SingletonIterator(value);
		return (T) createQueryByIterator(getSession(), hql, parameters).setMaxResults(1).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> findObjects(Class<T> clazz, String name, Object value, String... orderby) {
		String hql = "from " + clazz.getName() + " where " + name + "=?";
		if (value == null) {
			hql = "from " + clazz.getName() + " where " + name + " is null";
		} else if (value instanceof Object[]) {
			hql = "from " + clazz.getName() + " where " + name + " in (:list0)";
		} else if (value instanceof Collection) {
			hql = "from " + clazz.getName() + " where " + name + " in (:list0)";
		}
		if (orderby != null && orderby.length > 0) {
			hql = hql + " order by " + StringUtils.join(orderby, ",");
		}
		Iterator<?> parameters = (value == null) ? null : new SingletonIterator(value);
		return (List<T>) createQueryByIterator(getSession(), hql, parameters).list();
	}

	public List<?> findObjects(String hql, Object... parameters) {
		return createQuery(getSession(), hql, parameters).list();
	}

	public Object findUniqueObject(String hql, Object... parameters) {
		return createQuery(getSession(), hql, parameters).setMaxResults(1).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> findAllObjects(Class<T> clazz, String... orderby) {
		String hql = "from " + clazz.getName();
		if (orderby != null && orderby.length > 0) {
			hql = hql + " order by " + StringUtils.join(orderby, ",");
		}
		return (List<T>) findObjects(hql);
	}

	public <T> Pagelist findObjectsAsPage(Pagelist pagelist, Class<T> clazz, String... orderby) {
		String hql = "from " + clazz.getName();
		if (orderby != null && orderby.length > 0) {
			hql = hql + " order by " + StringUtils.join(orderby, ",");
		}
		return findObjectsAsPage(pagelist, hql);
	}

	public Pagelist findObjectsAsPage(Pagelist pagelist, String hql, Object... parameters) {
		Session session = getSession();

		if (pagelist.getCount() < 0) {
			String hql_count = hqlAsSelectCount(hql);
			Query query = createQuery(session, hql_count, parameters);
			int count = ((Number) query.uniqueResult()).intValue();
			pagelist.setCount(count);
		}

		List<?> items = Collections.EMPTY_LIST;
		if (pagelist.getCount() > 0) {
			Query query = createQuery(session, hql, parameters);
			query.setFirstResult(pagelist.getFirstResult());
			query.setMaxResults(pagelist.getPageSize());
			items = query.list();
		}
		pagelist.setItems(items);

		return pagelist;
	}

	public List<?> findObjectsByMax(int max, String hql, Object... parameters) {
		Query query = createQuery(getSession(), hql, parameters);
		query.setFirstResult(0);
		query.setMaxResults(max);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	private Query createQuery(Session session, String hql, Object... parameters) {
		if (parameters == null) {
			return createQueryByIterator(session, hql, null);
		}

		if (parameters.length == 1) {
			Object value = parameters[0];
			Class<? extends Object> clazz = value.getClass();
			if (ClassUtils.isAssignable(clazz, Map.class)) {
				return createQueryByMap(session, hql, (Map<String, Object>) value);
			} else if (ClassUtils.isAssignable(clazz, Collection.class)) {
				return createQueryByIterator(session, hql, new ArrayIterator(parameters));
			} else if (clazz.isPrimitive() || clazz.getName().startsWith("java.")) {
				return createQueryByIterator(session, hql, new ArrayIterator(parameters));
			} else {
				return createQueryByMap(session, hql, new BeanMap(value));
			}
		} else {
			return createQueryByIterator(session, hql, new ArrayIterator(parameters));
		}
	}

	private Query createQueryByMap(Session session, String hql, Map<String, Object> parameters) {
		Query query = createQueryByHql(session, hql);
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				String regex = "\\:" + key + "(\\s|\\)|$)";
				Pattern p = Pattern.compile(regex);
				if (p.matcher(hql).find()) {
					Object value = parameters.get(key);
					if (value == null) {
						query.setParameter(key, null);
					} else if (value instanceof Object[]) {
						query.setParameterList(key, (Object[]) value);
					} else if (value instanceof Collection) {
						query.setParameterList(key, (Collection<?>) value);
					} else {
						query.setParameter(key, value);
					}
				}
			}
		}
		return query;
	}

	private Query createQueryByIterator(Session session, String hql, Iterator<?> parameters) {
		Query query = createQueryByHql(session, hql);
		if (parameters != null) {

			Iterator<?> iterator = (Iterator<?>) parameters;
			int index = 0;
			while (iterator.hasNext()) {
				Object parameter = iterator.next();
				if (parameter != null) {
					if (parameter instanceof Object[]) {
						query.setParameterList("list" + index, (Object[]) parameter);
					} else if (parameter instanceof Collection) {
						query.setParameterList("list" + index, (Collection<?>) parameter);
					} else {
						query.setParameter(index, parameter);
					}
				} else {
					query.setParameter(index, null);
				}
				index++;
			}
		}
		return query;
	}

	private Query createQueryByHql(Session session, String hql) {
		if (hql.startsWith("sql:")) {
			return session.createSQLQuery(hql.substring(4));
		} else {
			return session.createQuery(hql);
		}
	}

	private String hqlAsSelectCount(String hql) {
		String countHql = hql.replaceAll("[\\n|\\r|\\t]", " ");
		int pos = countHql.toLowerCase().indexOf(" from ");
		countHql = countHql.substring(pos);

		pos = countHql.toLowerCase().lastIndexOf(" order by ");
		int lastpos = countHql.toLowerCase().lastIndexOf(")");
		if (pos != -1 && pos > lastpos) {
			countHql = countHql.substring(0, pos);
		}

		String regex = "((left|right|inner)\\s+)?+join\\s+(fetch\\s+)?\\w+(\\.\\w+)*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		countHql = p.matcher(countHql).replaceAll("");

		countHql = "select count(*) " + countHql;
		log.debug("hqlAsSelectCount = {}", countHql);
		return countHql;
	}

	public int executeUpdate(String hql, Object... parameters) {
		return createQuery(getSession(), hql, parameters).executeUpdate();
	}

	public String translateHQL(String hql) {
		QueryTranslatorImpl queryTranslator = new QueryTranslatorImpl(hql, hql, Collections.EMPTY_MAP, (SessionFactoryImplementor) sessionFactory);
		queryTranslator.compile(Collections.EMPTY_MAP, false);
		return queryTranslator.getSQLString();
	}

	public void unlazy(Object entity) {
		Hibernate.initialize(entity);
	}

	public void unlazy(Collection<?> entities, String attrNames) {
		for (Object entity : entities) {
			unlazy(entity, attrNames);
		}
	}

	public void unlazy(Object entity, String attrNames) {
		for (String name : attrNames.split(",")) {
			try {
				Object value = PropertyUtils.getProperty(entity, StringUtils.trim(name));

				if (value == null) {
					return;
				} else {
					Hibernate.initialize(value);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public int nextval(String name) {
		String hql = "sql: select " + name + ".nextval from dual";
		Number val = (Number) findUniqueObject(hql);
		return val.intValue();
	}
}

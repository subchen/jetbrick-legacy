package jetbrick.dao.schema.data.hibernate;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import jetbrick.commons.bean.ClassConvertUtils;
import jetbrick.dao.orm.ConnectionCallback;
import jetbrick.dao.schema.data.Pagelist;
import org.apache.commons.beanutils.*;
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

@SuppressWarnings("unchecked")
public class HibernateDaoHelper {
    protected static final Logger log = LoggerFactory.getLogger(HibernateDaoHelper.class);
    protected static final int LOAD_SOME_BATCH_SIZE = 200;
    protected final SessionFactory sessionFactory;

    public HibernateDaoHelper(SessionFactory sessionFactory) {
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

    public void flush() {
        getSession().flush();
    }

    // ----- execute -----------------------------------------------------
    public int execute(String hql, Object... parameters) {
        return createQuery(getSession(), hql, parameters).executeUpdate();
    }

    public void execute(final ConnectionCallback callback) {
        getSession().doWork(new Work() {
            @Override
            public void execute(Connection conn) throws SQLException {
                callback.execute(conn);
            }
        });
    }

    // ----- save/update/delete -----------------------------------------------------
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

    public void delete(Class<?> clazz, Serializable id) {
        delete(load(clazz, id));
    }

    // ----- batch save/update/delete -----------------------------------------------------
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

    public int deleteAll(Class<?> clazz, String name, Object value) {
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

    // ----- query -----------------------------------------------------

    public <T> T load(Class<T> clazz, Serializable id) {
        return (T) getSession().get(clazz, id);
    }

    public <T> T load(Class<T> clazz, String name, Object value) {
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

    // 如果数量超过 LOAD_SOME_BATCH_SIZE， 分批进行 load
    public <T> List<T> loadSome(Class<T> clazz, String name, Serializable... ids) {
        if (ids == null || ids.length == 0) {
            return Collections.<T> emptyList();
        }
        if (ids.length <= LOAD_SOME_BATCH_SIZE) {
            return loadSome(clazz, name, ids, 0, LOAD_SOME_BATCH_SIZE);
        }

        List<T> items = new ArrayList<T>(ids.length);
        int offset = 0;
        while (offset < ids.length) {
            List<T> some = loadSome(clazz, name, ids, offset, LOAD_SOME_BATCH_SIZE);
            items.addAll(some);
            offset += LOAD_SOME_BATCH_SIZE;
        }
        return items;
    }

    // load 固定大小的 内容 (从 offset开始最大载入limit数量)
    protected <T> List<T> loadSome(Class<T> clazz, String name, Serializable[] ids, int offset, int limit) {
        Serializable[] some_ids = ids;
        if (offset > 0 || limit < ids.length) {
            int length = Math.min(limit, ids.length - offset);
            some_ids = new Serializable[length];
            System.arraycopy(ids, offset, some_ids, 0, some_ids.length);
        }

        String values = StringUtils.repeat("?", ",", some_ids.length);
        String hql = "from " + clazz.getName() + " where " + name + " in (" + values + ")";
        return (List<T>) queryAsList(hql, (Object[]) some_ids);
    }

    public <T> List<T> loadAll(Class<T> clazz, String... sorts) {
        String hql = "from " + clazz.getName() + get_hql_sort_part(sorts);
        return (List<T>) queryAsList(hql);
    }

    public Object queryAsObject(String hql, Object... parameters) {
        return createQuery(getSession(), hql, parameters).setMaxResults(1).uniqueResult();
    }

    public Integer queryAsInt(String hql, Object... parameters) {
        return queryAsObjectCast(Integer.class, hql, parameters);
    }

    public Long queryAsLong(String hql, Object... parameters) {
        return queryAsObjectCast(Long.class, hql, parameters);
    }

    public String queryAsString(String hql, Object... parameters) {
        return queryAsObjectCast(String.class, hql, parameters);
    }

    public Boolean queryAsBoolean(String hql, Object... parameters) {
        return queryAsObjectCast(Boolean.class, hql, parameters);
    }

    public Date queryAsDate(String hql, Object... parameters) {
        return queryAsObjectCast(Date.class, hql, parameters);
    }

    protected <T> T queryAsObjectCast(Class<T> clazz, String hql, Object... parameters) {
        Object result = createQuery(getSession(), hql, parameters).setMaxResults(1).uniqueResult();
        return (result == null) ? null : (T) ConvertUtils.convert(result, clazz);
    }

    public <T> T[] queryAsArray(Class<T> arrayComponentClass, String hql, Object... parameters) {
        List<Object[]> list = (List<Object[]>) queryAsList(hql, parameters);
        int size = list == null ? 0 : list.size();
        T[] array = (T[]) Array.newInstance(arrayComponentClass, size);
        for (int i = 0; i < size; i++) {
            array[i] = ClassConvertUtils.convert(list.get(i)[0], arrayComponentClass);
        }
        return array;
    }

    public <T> List<T> queryAsList(Class<T> clazz, String name, Object value, String... sorts) {
        String hql = "from " + clazz.getName() + " where " + name + "=?";
        if (value == null) {
            hql = "from " + clazz.getName() + " where " + name + " is null";
        } else if (value instanceof Object[]) {
            hql = "from " + clazz.getName() + " where " + name + " in (:list0)";
        } else if (value instanceof Collection) {
            hql = "from " + clazz.getName() + " where " + name + " in (:list0)";
        }
        hql = hql + get_hql_sort_part(sorts);
        Iterator<?> parameters = (value == null) ? null : new SingletonIterator(value);
        return (List<T>) createQueryByIterator(getSession(), hql, parameters).list();
    }

    public List<?> queryAsList(String hql, Object... parameters) {
        return createQuery(getSession(), hql, parameters).list();
    }

    public List<?> queryAsList(int max, String hql, Object... parameters) {
        Query query = createQuery(getSession(), hql, parameters);
        query.setFirstResult(0);
        query.setMaxResults(max);
        return query.list();
    }

    public <T> Pagelist queryAsPagelist(Pagelist pagelist, Class<T> clazz, String... sorts) {
        String hql = "from " + clazz.getName() + get_hql_sort_part(sorts);
        return queryAsPagelist(pagelist, hql);
    }

    public Pagelist queryAsPagelist(Pagelist pagelist, String hql, Object... parameters) {
        Session session = getSession();

        if (pagelist.getCount() < 0) {
            String hql_count = get_hql_select_count(hql);
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

    private String get_hql_select_count(String hql) {
        String count_hql = hql.replaceAll("\\s+", " ");
        int pos = count_hql.toLowerCase().indexOf(" from ");
        count_hql = count_hql.substring(pos);

        pos = count_hql.toLowerCase().lastIndexOf(" order by ");
        int lastpos = count_hql.toLowerCase().lastIndexOf(")");
        if (pos != -1 && pos > lastpos) {
            count_hql = count_hql.substring(0, pos);
        }

        String regex = "(left|right|inner) join( fetch)?\\w+(\\.\\w+)*";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        count_hql = p.matcher(count_hql).replaceAll("");

        count_hql = "select count(*) " + count_hql;
        log.debug("get_hql_select_count = {}", count_hql);
        return count_hql;
    }

    private String get_hql_sort_part(String... sorts) {
        if (sorts == null || sorts.length == 0) {
            return "";
        }
        return " order by " + StringUtils.join(sorts, ",");
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
        return queryAsInt(hql).intValue();
    }
}

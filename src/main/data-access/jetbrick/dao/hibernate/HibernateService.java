package jetbrick.dao.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import jetbrick.dao.schema.data.Pagelist;

@SuppressWarnings("rawtypes")
public class HibernateService {
	protected HibernateDAO dao;

	public void setHibernateDAO(HibernateDAO dao) {
		this.dao = dao;
	}

	public <T> T getObject(Class<T> clazz, Serializable id) {
		return dao.getObject(clazz, id);
	}

	public void save(Object... entities) {
		dao.save(entities);
	}

	public void saveAll(Collection entities) {
		dao.saveAll(entities);
	}

	public void saveOrUpdate(Object... entities) {
		dao.saveOrUpdate(entities);
	}

	public void saveOrUpdateAll(Collection entities) {
		dao.saveOrUpdateAll(entities);
	}

	public void update(Object... entities) {
		dao.update(entities);
	}

	public void updateAll(Collection entities) {
		dao.updateAll(entities);
	}

	public void delete(Object... entities) {
		dao.delete(entities);
	}

	public void deleteAll(Collection entities) {
		dao.deleteAll(entities);
	}

	public <T> void deleteObject(Class<T> clazz, Serializable id) {
		dao.deleteObject(clazz, id);
	}

	public int deleteObjects(Class clazz, String name, Object value) {
		return dao.deleteObjects(clazz, name, value);
	}

	public <T> T findObject(Class<T> clazz, String name, Object value) {
		return dao.findObject(clazz, name, value);
	}

	public <T> List<T> findObjects(Class<T> clazz, String name, Object value, String... orderby) {
		return dao.findObjects(clazz, name, value, orderby);
	}

	public <T> List<T> findAllObjects(Class<T> clazz, String... orderby) {
		return dao.findAllObjects(clazz, orderby);
	}

	public List findObjects(String hql, Object... parameters) {
		return dao.findObjects(hql, parameters);
	}

	public Object findUniqueObject(String hql, Object... parameters) {
		return dao.findUniqueObject(hql, parameters);
	}

	public <T> Pagelist findObjectsAsPage(Pagelist pagelist, Class<T> clazz, String... orderby) {
		return dao.findObjectsAsPage(pagelist, clazz, orderby);
	}

	public Pagelist findObjectsAsPage(Pagelist pagelist, String hql, Object... parameters) {
		return dao.findObjectsAsPage(pagelist, hql, parameters);
	}

	public List findObjectsByMax(int max, String hql, Object... parameters) {
		return dao.findObjectsByMax(max, hql, parameters);
	}

	public int executeUpdate(String hql, Object... parameters) {
		return dao.executeUpdate(hql, parameters);
	}

	public void unlazy(Collection entitys, String attrNames) {
		dao.unlazy(entitys, attrNames);
	}

	public void unlazy(Object entity, String attrNames) {
		dao.unlazy(entity, attrNames);
	}

	/**
	 * 得到 Sequence 值
	 */
	public int nextval(String name) {
		return dao.nextval(name);
	}

	/**
	 * 实现外部操作变成一个Database事务
	 */
	public void execute(HibernateTransaction trans) {
		trans.execute(dao);
	}

	/**
	 * 允许使用原始的JDBC操作, 在JdbcTransaction中可以获得原始的Connection
	 */
	public void execute(JdbcTransaction trans) {
		dao.execute(trans);
	}
}

package jetbrick.dao.schema.data.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import jetbrick.dao.orm.Pagelist;
import jetbrick.dao.schema.data.*;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unchecked")
public class HibernateEntityDaoHelper<T extends Entity> implements EntityDaoHelper<T> {
    protected final HibernateDaoHelper dao;
    protected final Class<T> entityClass;
    protected final SchemaInfo<T> schema;
    protected final String tableNameIdentifier;
    protected final String hql_delete;

    public HibernateEntityDaoHelper(HibernateDaoHelper dao, Class<T> entityClass) {
        this.dao = dao;
        this.entityClass = entityClass;
        this.schema = EntityUtils.getSchema(entityClass);
        this.tableNameIdentifier = schema.getTableClass().getSimpleName();
        this.hql_delete = "sql:" + EntitySqlUtils.get_sql_delete(schema, dao.getDialect());
    }

    //------ table ----------------------------------
    @Override
    public boolean tableExist() {
        return dao.tableExist(schema.getTableName());
    }

    @Override
    public int tableCreate() {
        String sql = EntitySqlUtils.get_sql_table_create(schema, dao.getDialect());
        return dao.execute("sql:" + sql);
    }

    @Override
    public int tableDelete() {
        String sql = "drop table " + tableNameIdentifier;
        return dao.execute(sql);
    }

    // -------- save/update/delete ---------------------------------
    @Override
    public int save(T entity) {
        entity.validate();
        entity.generateId();
        dao.save(entity);
        return 1;
    }

    @Override
    public int update(T entity) {
        entity.validate();
        dao.update(entity);
        return 1;
    }

    @Override
    public int saveOrUpdate(T entity) {
        if (entity.getId() == null) {
            return save(entity);
        } else {
            return update(entity);
        }
    }

    @Override
    public int delete(T entity) {
        return delete(entity.getId());
    }

    @Override
    public int delete(Integer id) {
        return dao.execute(hql_delete, id);
    }

    // -------- batch save/update/delete ---------------------------------
    @Override
    public void saveAll(Collection<T> entities) {
        if (entities == null || entities.size() == 0) return;

        for (T entity : entities) {
            entity.generateId();
            entity.validate();
            dao.save(entity);
        }
        dao.flush();
    }

    @Override
    public void updateAll(Collection<T> entities) {
        if (entities == null || entities.size() == 0) return;

        for (T entity : entities) {
            entity.validate();
            dao.update(entity);
        }
        dao.flush();
    }

    @Override
    public void saveOrUpdateAll(Collection<T> entities) {
        if (entities == null || entities.size() == 0) return;

        for (T entity : entities) {
            if (entity.getId() == null) {
                save(entity);
            } else {
                update(entity);
            }
        }
        dao.flush();
    }

    @Override
    public void deleteAll(Collection<T> entities) {
        if (entities == null || entities.size() == 0) return;

        int i = 0;
        Integer[] ids = new Integer[entities.size()];
        for (T entity : entities) {
            ids[i++] = entity.getId();
        }
        deleteAll(ids);
    }

    @Override
    public int deleteAll(Integer... ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        String values = StringUtils.repeat("?", ",", ids.length);
        String hql = "delete from " + tableNameIdentifier + " where id in (" + values + ")";
        return dao.execute(hql, (Object[]) ids);
    }

    // -------- load ---------------------------------
    @Override
    public T load(Integer id) {
        return dao.load(entityClass, id);
    }

    @Override
    public T load(String name, Object value) {
        String hql = "from " + tableNameIdentifier + " where " + name + "=?";
        return queryAsObject(hql, value);
    }

    @Override
    public List<T> loadSome(Integer... ids) {
        return (List<T>) dao.loadSome(entityClass, "id", (Serializable[]) ids);
    }

    @Override
    public List<T> loadSome(String name, Object value, String... sorts) {
        String hql = "from " + tableNameIdentifier + " where " + name + "=?" + get_hql_sort_part(sorts);
        return queryAsList(hql, value);
    }

    @Override
    public List<T> loadAll(String... sorts) {
        String hql = "from " + tableNameIdentifier;
        if (sorts != null && sorts.length > 0) {
            hql = hql + " order by " + get_hql_sort_part(sorts);
        }
        return queryAsList(hql);
    }

    // -------- query ---------------------------------
    @Override
    public T queryAsObject(String hql, Object... parameters) {
        return (T) dao.queryAsObject(hql, parameters);
    }

    @Override
    public List<T> queryAsList(String hql, Object... parameters) {
        return (List<T>) dao.queryAsList(hql, parameters);
    }

    @Override
    public Pagelist queryAsPagelist(Pagelist pagelist, String hql, Object... parameters) {
        return dao.queryAsPagelist(pagelist, hql, parameters);
    }

    // ----- hql gen ---------------------------------------------
    private String get_hql_sort_part(String... sorts) {
        if (sorts == null || sorts.length == 0) {
            return "";
        }
        return " order by " + StringUtils.join(sorts, ",");
    }

}

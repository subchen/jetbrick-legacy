package jetbrick.dao.schema.data.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import jetbrick.commons.lang.ObjectHolder;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.ConnectionCallback;
import jetbrick.dao.orm.utils.JdbcUtils;
import jetbrick.dao.schema.data.*;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unchecked")
public class HibernateEntityDaoHelper<T extends Entity> {
    protected final HibernateDaoHelper dao;
    protected final Dialect dialect;
    protected final Class<T> entityClass;
    protected final SchemaInfo<T> schema;
    protected final String tableNameIdentifier;
    protected final String hql_delete;

    public HibernateEntityDaoHelper(HibernateDaoHelper dao, Class<T> entityClass) {
        this.dao = dao;
        this.dialect = doGetDialect();
        this.entityClass = entityClass;
        this.schema = EntityUtils.getSchema(entityClass);
        this.tableNameIdentifier = schema.getTableClass().getSimpleName();
        this.hql_delete = "sql:" + EntitySqlUtils.get_sql_delete(schema, dialect);
    }

    //------ table ----------------------------------
    public boolean tableExist() {
        final ObjectHolder<Boolean> result = new ObjectHolder<Boolean>();
        dao.execute(new ConnectionCallback() {
            @Override
            public void execute(Connection conn) throws SQLException {
                result.put(JdbcUtils.doGetTableExist(conn, schema.getTableName()));
            }
        });
        return result.get();
    }

    public int tableCreate() {
        String sql = EntitySqlUtils.get_sql_table_create(schema, dialect);
        return dao.execute("sql:" + sql);
    }

    public int tableDelete() {
        String sql = "drop table " + tableNameIdentifier;
        return dao.execute(sql);
    }

    // -------- save/update/delete ---------------------------------
    public int save(T entity) {
        entity.validate();
        entity.generateId();
        dao.save(entity);
        return 1;
    }

    public int update(T entity) {
        entity.validate();
        dao.update(entity);
        return 1;
    }

    public int saveOrUpdate(T entity) {
        if (entity.getId() == null) {
            return save(entity);
        } else {
            return update(entity);
        }
    }

    public int delete(T entity) {
        return delete(entity.getId());
    }

    public int delete(Integer id) {
        return dao.execute(hql_delete, id);
    }

    // -------- batch save/update/delete ---------------------------------
    public void saveAll(T... entities) {
        if (entities.length == 0) return;

        for (T entity : entities) {
            entity.generateId();
            entity.validate();
            dao.save(entity);
        }

        dao.flush();
    }

    public void updateAll(T... entities) {
        if (entities.length == 0) return;

        for (T entity : entities) {
            entity.validate();
            dao.update(entity);
        }

        dao.flush();
    }

    public void saveOrUpdateAll(T... entities) {
        if (entities.length == 0) return;

        for (T entity : entities) {
            if (entity.getId() == null) {
                save(entity);
            } else {
                update(entity);
            }
        }
    }

    public void deleteAll(T... entities) {
        if (entities.length == 0) return;

        Integer[] ids = new Integer[entities.length];
        for (int i = 0; i < entities.length; i++) {
            ids[i] = entities[i].getId();
        }

        deleteAll(ids);
    }

    public int deleteAll(Integer... ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        String values = StringUtils.repeat("?", ",", ids.length);
        String hql = "delete from " + tableNameIdentifier + " where id in (" + values + ")";
        return dao.execute(hql, (Object[]) ids);
    }

    // -------- load ---------------------------------
    public T load(Integer id) {
        return dao.load(entityClass, id);
    }

    public T load(String name, Object value) {
        String hql = "from " + tableNameIdentifier + " where " + name + "=?";
        return queryAsObject(hql, value);
    }

    public List<T> loadSome(Integer... ids) {
        return (List<T>) dao.loadSome(entityClass, "id", (Serializable[]) ids);
    }

    public List<T> loadSome(String name, Object value, String... sorts) {
        String hql = "from " + tableNameIdentifier + " where " + name + "=?" + get_hql_sort_part(sorts);
        return queryAsList(hql, value);
    }

    public List<T> loadAll(String... sorts) {
        String hql = "from " + tableNameIdentifier;
        if (sorts != null && sorts.length > 0) {
            hql = hql + " order by " + get_hql_sort_part(sorts);
        }
        return queryAsList(hql);
    }

    // -------- query ---------------------------------
    public T queryAsObject(String hql, Object... parameters) {
        return (T) dao.queryAsObject(hql, parameters);
    }

    public List<T> queryAsList(String hql, Object... parameters) {
        return (List<T>) dao.queryAsList(hql, parameters);
    }

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

    private Dialect doGetDialect() {
        final ObjectHolder<Dialect> result = new ObjectHolder<Dialect>();
        dao.execute(new ConnectionCallback() {
            @Override
            public void execute(Connection conn) {
                result.put(JdbcUtils.doGetDialet(conn));
            }
        });
        return result.get();
    }
}

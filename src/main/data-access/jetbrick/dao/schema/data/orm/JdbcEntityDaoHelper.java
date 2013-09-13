package jetbrick.dao.schema.data.orm;

import java.util.*;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.JdbcHelper;
import jetbrick.dao.orm.RowMapper;
import jetbrick.dao.schema.data.*;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unchecked")
public class JdbcEntityDaoHelper<T extends Entity> implements EntityDaoHelper<T> {
    protected final JdbcHelper jdbc;
    protected final Dialect dialect;
    protected final Class<T> entityClass;
    protected final SchemaInfo<T> schema;
    protected final RowMapper<T> rowMapper;
    protected final String tableNameIdentifier;
    protected final String sql_insert;
    protected final String sql_update;
    protected final String sql_delete;
    protected final String sql_select;

    public JdbcEntityDaoHelper(JdbcHelper jdbc, Class<T> entityClass) {
        this.jdbc = jdbc;
        this.dialect = jdbc.getDialect();
        this.entityClass = entityClass;
        this.schema = EntityUtils.getSchema(entityClass);
        this.rowMapper = EntityUtils.getEntityRowMapper(entityClass);
        this.tableNameIdentifier = dialect.getIdentifier(schema.getTableName());
        this.sql_insert = EntitySqlUtils.get_sql_insert(schema, dialect);
        this.sql_update = EntitySqlUtils.get_sql_update(schema, dialect);
        this.sql_delete = EntitySqlUtils.get_sql_delete(schema, dialect);
        this.sql_select = EntitySqlUtils.get_sql_select_object(schema, dialect);
    }

    // -------- table ---------------------------------
    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#tableExist()
     */
    @Override
    public boolean tableExist() {
        return jdbc.tableExist(schema.getTableName());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#tableCreate()
     */
    @Override
    public int tableCreate() {
        String sql = EntitySqlUtils.get_sql_table_create(schema, dialect);
        return jdbc.execute(sql);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#tableDelete()
     */
    @Override
    public int tableDelete() {
        String sql = "drop table " + tableNameIdentifier;
        return jdbc.execute(sql);
    }

    // -------- save/update/delete ---------------------------------
    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#save(T)
     */
    @Override
    public int save(T entity) {
        entity.validate();
        entity.generateId();
        return jdbc.execute(sql_insert, entity.dao_insert_parameters());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#update(T)
     */
    @Override
    public int update(T entity) {
        entity.validate();
        return jdbc.execute(sql_update, entity.dao_update_parameters());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#saveOrUpdate(T)
     */
    @Override
    public int saveOrUpdate(T entity) {
        if (entity.getId() == null) {
            return save(entity);
        } else {
            return update(entity);
        }
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#delete(T)
     */
    @Override
    public int delete(T entity) {
        return delete(entity.getId());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#delete(java.lang.Integer)
     */
    @Override
    public int delete(Integer id) {
        return jdbc.execute(sql_delete, id);
    }

    // -------- batch save/update/delete ---------------------------------
    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#saveAll(T)
     */
    @Override
    public void saveAll(T... entities) {
        if (entities.length == 0) return;

        List<Object[]> parameters = new ArrayList<Object[]>(entities.length);
        for (T entity : entities) {
            entity.generateId();
            entity.validate();
            parameters.add(entity.dao_insert_parameters());
        }

        jdbc.executeBatch(sql_insert, parameters);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#saveAll(java.util.List)
     */
    @Override
    public void saveAll(List<T> entities) {
        saveAll((T[]) entities.toArray());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#updateAll(T)
     */
    @Override
    public void updateAll(T... entities) {
        if (entities.length == 0) return;

        List<Object[]> parameters = new ArrayList<Object[]>(entities.length);
        for (T entity : entities) {
            entity.validate();
            parameters.add(entity.dao_update_parameters());
        }

        jdbc.executeBatch(sql_update, parameters);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#updateAll(java.util.List)
     */
    @Override
    public void updateAll(List<T> entities) {
        updateAll((T[]) entities.toArray());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#saveOrUpdateAll(T)
     */
    @Override
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

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#saveOrUpdateAll(java.util.List)
     */
    @Override
    public void saveOrUpdateAll(List<T> entities) {
        saveOrUpdateAll((T[]) entities.toArray());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#deleteAll(T)
     */
    @Override
    public void deleteAll(T... entities) {
        if (entities.length == 0) return;

        Integer[] ids = new Integer[entities.length];
        for (int i = 0; i < entities.length; i++) {
            ids[i] = entities[i].getId();
        }

        deleteAll(ids);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#deleteAll(java.util.List)
     */
    @Override
    public void deleteAll(List<T> entities) {
        deleteAll((T[]) entities.toArray());
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#deleteAll(java.lang.Integer)
     */
    @Override
    public int deleteAll(Integer... ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        String values = StringUtils.repeat("?", ",", ids.length);
        String sql = "delete from " + tableNameIdentifier + " where id in (" + values + ")";
        return jdbc.execute(sql, (Object[]) ids);
    }

    // -------- load ---------------------------------
    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#load(java.lang.Integer)
     */
    @Override
    public T load(Integer id) {
        return jdbc.queryAsObject(rowMapper, sql_select, id);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#load(java.lang.String, java.lang.Object)
     */
    @Override
    public T load(String name, Object value) {
        String sql = "select * from " + tableNameIdentifier + " where " + getColumnNameIdentifier(name) + "=?";
        return queryAsObject(sql, value);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#loadSome(java.lang.Integer)
     */
    @Override
    public List<T> loadSome(Integer... ids) {
        if (ids == null || ids.length == 0) {
            return Collections.<T> emptyList();
        }
        String values = StringUtils.repeat("?", ",", ids.length);
        String sql = "select * from " + tableNameIdentifier + " where id in (" + values + ")";
        return jdbc.queryAsList(rowMapper, sql, (Object[]) ids);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#loadSome(java.lang.String, java.lang.Object, java.lang.String)
     */
    @Override
    public List<T> loadSome(String name, Object value, String... sorts) {
        //@formatter:off
		String sql = "select * from " + tableNameIdentifier 
				   + " where " + getColumnNameIdentifier(name) + "=?"
				   + get_sql_sort_part(sorts);
		//@formatter:on
        return queryAsList(sql, value);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#loadAll(java.lang.String)
     */
    @Override
    public List<T> loadAll(String... sorts) {
        String sql = "select * from " + tableNameIdentifier + get_sql_sort_part(sorts);
        return queryAsList(sql);
    }

    // -------- query ---------------------------------
    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#queryAsObject(java.lang.String, java.lang.Object)
     */
    @Override
    public T queryAsObject(String sql, Object... parameters) {
        return jdbc.queryAsObject(rowMapper, sql, parameters);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#queryAsList(java.lang.String, java.lang.Object)
     */
    @Override
    public List<T> queryAsList(String sql, Object... parameters) {
        return jdbc.queryAsList(rowMapper, sql, parameters);
    }

    /* (non-Javadoc)
     * @see jetbrick.dao.schema.data.orm.BasicEntityDaoHelper#queryAsPagelist(jetbrick.dao.schema.data.Pagelist, java.lang.String, java.lang.Object)
     */
    @Override
    public Pagelist queryAsPagelist(Pagelist pagelist, String sql, Object... parameters) {
        return jdbc.queryAsPagelist(pagelist, rowMapper, sql, parameters);
    }

    // ----- sql gen ---------------------------------------------
    private String get_sql_sort_part(String... sorts) {
        if (sorts == null || sorts.length == 0) {
            return "";
        }
        for (int i = 0; i < sorts.length; i++) {
            String part[] = StringUtils.split(sorts[i], " ");
            part[0] = getColumnNameIdentifier(part[0]);
            sorts[i] = StringUtils.join(part, " ");
        }
        return " order by " + StringUtils.join(sorts, ",");
    }

    private String getColumnNameIdentifier(String name) {
        if (name.indexOf("_") == -1) {
            // maybe fieldName
            SchemaColumn sc = schema.getColumn(name);
            if (sc != null) {
                name = sc.getColumnName();
            }
        }
        return dialect.getIdentifier(name);
    }
}
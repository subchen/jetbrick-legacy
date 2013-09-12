package jetbrick.dao.schema.data;

import java.util.ArrayList;
import java.util.List;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.orm.JdbcHelper;
import jetbrick.dao.orm.RowMapper;
import org.apache.commons.lang3.StringUtils;

public class EntityDaoHelper<T extends Entity> {
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

    public EntityDaoHelper(JdbcHelper jdbc, Class<T> entityClass) {
        this.jdbc = jdbc;
        this.dialect = jdbc.getDialect();
        this.entityClass = entityClass;
        this.schema = EntityUtils.getSchema(entityClass);
        this.rowMapper = EntityUtils.getEntityRowMapper(entityClass);
        this.tableNameIdentifier = dialect.getIdentifier(schema.getTableName());
        this.sql_insert = get_sql_insert();
        this.sql_update = get_sql_update();
        this.sql_delete = get_sql_delete();
        this.sql_select = get_sql_select();
    }

    // -------- table ---------------------------------
    public boolean tableExist() {
        return jdbc.tableExist(schema.getTableName());
    }

    public int tableCreate() {
        String sql = get_sql_table_create();
        return jdbc.execute(sql);
    }

    public int tableDelete() {
        String sql = "drop table " + tableNameIdentifier;
        return jdbc.execute(sql);
    }

    // -------- save/update/delete ---------------------------------
    public int save(T entity) {
        entity.validate();
        entity.generateId();
        return jdbc.execute(sql_insert, entity.dao_insert_parameters());
    }

    public int update(T entity) {
        entity.validate();
        return jdbc.execute(sql_update, entity.dao_update_parameters());
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
        return jdbc.execute(sql_delete, id);
    }

    // -------- batch save/update/delete ---------------------------------
    public void saveAll(T... entities) {
        if (entities.length == 0) return;

        List<Object[]> parameters = new ArrayList(entities.length);
        for (T entity : entities) {
            entity.generateId();
            entity.validate();
            parameters.add(entity.dao_insert_parameters());
        }

        jdbc.executeBatch(sql_insert, parameters);
    }

    public void saveAll(List<T> entities) {
        saveAll((T[]) entities.toArray());
    }

    public void updateAll(T... entities) {
        if (entities.length == 0) return;

        List<Object[]> parameters = new ArrayList(entities.length);
        for (T entity : entities) {
            entity.validate();
            parameters.add(entity.dao_update_parameters());
        }

        jdbc.executeBatch(sql_update, parameters);
    }

    public void updateAll(List<T> entities) {
        updateAll((T[]) entities.toArray());
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

    public void saveOrUpdateAll(List<T> entities) {
        saveOrUpdateAll((T[]) entities.toArray());
    }

    public void deleteAll(T... entities) {
        if (entities.length == 0) return;

        Integer[] ids = new Integer[entities.length];
        for (int i = 0; i < entities.length; i++) {
            ids[i] = entities[i].getId();
        }

        deleteAll(ids);
    }

    public void deleteAll(List<T> entities) {
        deleteAll((T[]) entities.toArray());
    }

    public int deleteAll(Integer... ids) {
        String values = StringUtils.repeat("?", ",", ids.length);
        String sql = "delete from " + tableNameIdentifier + " where id in (" + values + ")";
        return jdbc.execute(sql, (Object[]) ids);
    }

    // -------- load/query ---------------------------------
    public T load(Integer id) {
        return jdbc.queryAsObject(rowMapper, sql_select, id);
    }

    public T load(String name, Object value) {
        String sql = "select * from " + tableNameIdentifier + " where " + getColumnNameIdentifier(name) + "=?";
        return queryAsObject(sql, value);
    }

    public List<T> loadSome(Integer... ids) {
        String values = StringUtils.repeat("?", ",", ids.length);
        String sql = "select * from " + tableNameIdentifier + " where id in (" + values + ")";
        return jdbc.queryAsList(rowMapper, sql, (Object[]) ids);
    }

    public List<T> loadSome(String name, Object value, String... sorts) {
        String sql = "select * from " + tableNameIdentifier + " where " + getColumnNameIdentifier(name) + "=?";
        if (sorts != null && sorts.length > 0) {
            sql = sql + " order by " + get_sql_sort_part(sorts);
        }
        return queryAsList(sql, value);
    }

    public List<T> loadAll(String... sorts) {
        String sql = "select * from " + tableNameIdentifier;
        if (sorts != null && sorts.length > 0) {
            sql = sql + " order by " + get_sql_sort_part(sorts);
        }
        return queryAsList(sql);
    }

    public T queryAsObject(String sql, Object... parameters) {
        return jdbc.queryAsObject(rowMapper, sql, parameters);
    }

    public List<T> queryAsList(String sql, Object... parameters) {
        return jdbc.queryAsList(rowMapper, sql, parameters);
    }

    public Pagelist queryAsPageList(Pagelist pagelist, String sql, Object... parameters) {
        return jdbc.queryAsPageList(pagelist, rowMapper, sql, parameters);
    }

    // ----- sql gen ---------------------------------------------
    private String get_sql_insert() {
        List<String> names = new ArrayList<String>();
        for (SchemaColumn c : schema.getColumns()) {
            names.add(dialect.getIdentifier(c.getColumnName()));
        }
        String sql = "insert into %s (%s) values (%s)";
        //@formatter:off
        return String.format(sql, 
            tableNameIdentifier,
            StringUtils.join(names, ","), 
            StringUtils.repeat("?", ",", names.size())
        );
        //@formatter:on
    }

    private String get_sql_update() {
        List<String> names = new ArrayList<String>();
        for (SchemaColumn c : schema.getColumns()) {
            if (!c.isPrimaryKey()) {
                names.add(dialect.getIdentifier(c.getColumnName()) + "=?");
            }
        }
        String sql = "update %s set %s where id=?";
        return String.format(sql, tableNameIdentifier, StringUtils.join(names, ","));
    }

    private String get_sql_delete() {
        return "delete from " + tableNameIdentifier + " where id=?";
    }

    private String get_sql_select() {
        return "se3lect * from " + tableNameIdentifier + " where id=?";
    }

    private String get_sql_table_create() {
        StringBuilder sqls = new StringBuilder();
        List<String> pks = new ArrayList<String>(3);

        sqls.append("create table " + tableNameIdentifier + " (\n");
        for (SchemaColumn c : schema.getColumns()) {
            if (c.isPrimaryKey()) {
                pks.add(dialect.getIdentifier(c.getColumnName()));
            }
            sqls.append("    ");
            sqls.append(dialect.getIdentifier(c.getColumnName()));
            sqls.append(" ");
            sqls.append(dialect.asSqlType(c.getTypeName(), c.getTypeLength(), c.getTypeScale()));
            sqls.append(c.isNullable() ? "" : " not null");
            sqls.append(",\n");
        }

        sqls.append("    primary key (" + StringUtils.join(pks, ",") + ")\n");
        sqls.append(");\n");

        return sqls.toString();
    }

    private String get_sql_sort_part(String... sorts) {
        for (int i = 0; i < sorts.length; i++) {
            String part[] = StringUtils.split(sorts[i], " ");
            part[0] = getColumnNameIdentifier(part[0]);
            sorts[i] = StringUtils.join(part, " ");
        }
        return StringUtils.join(sorts, ",");
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

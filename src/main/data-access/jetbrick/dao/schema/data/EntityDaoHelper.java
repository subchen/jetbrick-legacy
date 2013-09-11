package jetbrick.dao.schema.data;

import java.util.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.dao.oam.JdbcTemplate;
import jetbrick.dao.oam.RowMapper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class EntityDaoHelper extends SqlDaoHelper {
    protected static final Map<Class<? extends Entity>, String> sql_cache_insert = new HashMap(50);
    protected static final Map<Class<? extends Entity>, String> sql_cache_update = new HashMap(50);
    protected static final Map<String, String> sql_cache_delete = new HashMap(50);

    public EntityDaoHelper(JdbcTemplate jdbc) {
        super(jdbc);
    }
    
    // -------- table ---------------------------------
    public boolean tableExist(String name) {
        return jdbc.tableExist(name);
    }

    public int tableCreate(SchemaInfo<? extends Entity> schema) {
        String sql = get_sql_table_create(schema);
        return jdbc.execute(sql);
    }

    public int tableDelete(String tableName) {
        String sql = sql_cache_delete.get(tableName);
        if (sql == null) {
            sql = get_sql_table_drop(tableName);
            sql_cache_delete.put(tableName, sql);
        }
        return jdbc.execute(sql);
    }

    // -------- save/update/delete ---------------------------------
    public int save(Entity entity) {
        entity.validate();
        String sql = sql_cache_insert.get(entity.getClass());
        if (sql == null) {
            sql = get_sql_insert(entity.getSchema());
            sql_cache_insert.put(entity.getClass(), sql);
        }
        entity.generateId();
        return jdbc.execute(sql, entity.dao_insert_parameters());
    }

    public int update(Entity entity) {
        entity.validate();
        String sql = sql_cache_update.get(entity.getClass());
        if (sql == null) {
            sql = get_sql_update(entity.getSchema());
            sql_cache_update.put(entity.getClass(), sql);
        }
        return jdbc.execute(sql, entity.dao_update_parameters());
    }

    public int saveOrUpdate(Entity entity) {
        if (entity.getId() == null) {
            return save(entity);
        } else {
            return update(entity);
        }
    }

    public int delete(Entity entity) {
        String tableName = dialect.getIdentifier(entity.getSchema().getTableName());
        String sql = "delete from " + tableName + " where id=?";
        return jdbc.execute(sql, entity.getId());
    }

    public int delete(Class<? extends Entity> entityClass, Integer id) {
        String tableName = EntityUtils.getTableName(entityClass);
        String sql = "delete from " + dialect.getIdentifier(tableName) + " where id=?";
        return jdbc.execute(sql, id);
    }

    // -------- batch save/update/delete ---------------------------------
    public int[] saveAll(Entity... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = save(datalist[i]);
        }
        return results;
    }

    public int[] updateAll(Entity... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = update(datalist[i]);
        }
        return results;
    }

    public int[] saveOrUpdateAll(Entity... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = saveOrUpdate(datalist[i]);
        }
        return results;
    }

    public int[] deleteAll(Entity... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = delete(datalist[i]);
        }
        return results;
    }

    /**
     * 批量保存， 必须保证对象是相同的Class
     */
    public <T extends Entity> int[] saveBatch(List<T> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        Entity first = dataList.get(0);
        String sql = sql_cache_insert.get(first.getClass());
        if (sql == null) {
            sql = get_sql_insert(first.getSchema());
            sql_cache_insert.put(first.getClass(), sql);
        }

        List<Object[]> parameters = new ArrayList(dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            Entity data = dataList.get(i);
            data.generateId();
            data.validate();

            if (data.getSchema() != first.getSchema()) {
                throw new SystemException("dataList 存在不同的 Class， 无法进行 batchSave");
            }
            parameters.add(data.dao_insert_parameters());
        }

        return jdbc.executeBatch(sql, parameters);
    }

    /**
     * 批量更新， 必须保证对象是相同的Class
     */
    public <T extends Entity> int[] updateBatch(List<T> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        Entity first = dataList.get(0);
        String sql = sql_cache_update.get(first.getClass());
        if (sql == null) {
            sql = get_sql_update(first.getSchema());
            sql_cache_update.put(first.getClass(), sql);
        }

        List<Object[]> parameters = new ArrayList(dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            Entity data = dataList.get(i);
            data.validate();

            if (data.getSchema() != first.getSchema()) {
                throw new SystemException("dataList 存在不同的 Class， 无法进行 batchSave");
            }
            parameters.add(data.dao_update_parameters());
        }

        return jdbc.executeBatch(sql, parameters);
    }

    // -------- query ---------------------------------
    public <T extends Entity> T load(Class<T> entityClass, Integer id) {
        String tableName = EntityUtils.getTableName(entityClass);
        String sql = "select * from " + dialect.getIdentifier(tableName) + " where id=?";
        return queryAsObject(entityClass, sql, id);
    }

    public <T extends Entity> T load(Class<T> entityClass, String name, Object value) {
        String tableName = EntityUtils.getTableName(entityClass);
        String columnName = EntityUtils.getColumnName(entityClass, name);

        String sql = "select * from " + dialect.getIdentifier(tableName) + " where " + dialect.getIdentifier(columnName) + "=?";
        return queryAsObject(entityClass, sql, value);
    }

    public <T extends Entity> List<T> loadSome(Class<T> entityClass, String name, Object value, String... sorts) {
        String tableName = EntityUtils.getTableName(entityClass);
        String columnName = EntityUtils.getColumnName(entityClass, name);

        String sql = "select * from " + dialect.getIdentifier(tableName) + " where " + dialect.getIdentifier(columnName) + "=?";
        if (sorts != null && sorts.length > 0) {
            sql = sql + " order by " + get_sql_sort_part(entityClass, sorts);
        }
        return queryAsList(entityClass, sql, value);
    }

    public <T extends Entity> List<T> loadSome(Class<T> entityClass, Integer... ids) {
        String tableName = EntityUtils.getTableName(entityClass);
        String values = StringUtils.repeat("?", ",", ids.length);
        String sql = "select * from " + dialect.getIdentifier(tableName) + " where id in (" + values + ")";
        return queryAsList(entityClass, sql, (Object[]) ids);
    }

    public <T extends Entity> List<T> loadAll(Class<T> entityClass, String... sorts) {
        String tableName = EntityUtils.getTableName(entityClass);
        String sql = "select * from " + dialect.getIdentifier(tableName);
        if (sorts != null && sorts.length > 0) {
            sql = sql + " order by " + get_sql_sort_part(entityClass, sorts);
        }
        return queryAsList(entityClass, sql);
    }

    public <T extends Entity> T queryAsObject(Class<T> entityClass, String sql, Object... parameters) {
        RowMapper<T> mapper = EntityUtils.getEntityRowMapper(entityClass);
        return jdbc.queryAsObject(mapper, sql, parameters);
    }

    public <T extends Entity> List<T> queryAsList(Class<T> entityClass, String sql, Object... parameters) {
        RowMapper<T> mapper = EntityUtils.getEntityRowMapper(entityClass);
        return jdbc.queryAsList(mapper, sql, parameters);
    }

    // ----- sql gen ---------------------------------------------
    private String get_sql_insert(SchemaInfo<? extends Entity> schema) {
        List<String> names = new ArrayList<String>();
        for (SchemaColumn c : schema.getColumns()) {
            names.add(dialect.getIdentifier(c.getColumnName()));
        }
        String sql = "insert into %s (%s) values (%s)";
        //@formatter:off
        return String.format(sql, 
            dialect.getIdentifier(schema.getTableName()), 
            StringUtils.join(names, ","), 
            StringUtils.repeat("?", ",", names.size())
        );
        //@formatter:on
    }

    private String get_sql_update(SchemaInfo<? extends Entity> schema) {
        List<String> names = new ArrayList<String>();
        for (SchemaColumn c : schema.getColumns()) {
            if (!c.isPrimaryKey()) {
                names.add(dialect.getIdentifier(c.getColumnName()) + "=?");
            }
        }
        String sql = "update %s set %s where id=?";
        return String.format(sql, dialect.getIdentifier(schema.getTableName()), StringUtils.join(names, ","));
    }

    private String get_sql_table_create(SchemaInfo<? extends Entity> schema) {
        StringBuilder sqls = new StringBuilder();
        List<String> pks = new ArrayList<String>(3);

        sqls.append("create table " + dialect.getIdentifier(schema.getTableName()) + " (\n");
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

    private String get_sql_table_drop(String tableName) {
        return "drop table " + dialect.getIdentifier(tableName);
    }

    private String get_sql_sort_part(Class<? extends Entity> entityClass, String... sorts) {
        for (int i = 0; i < sorts.length; i++) {
            String part[] = StringUtils.split(sorts[i], " ");
            part[0] = EntityUtils.getColumnName(entityClass, part[0]);
            sorts[i] = StringUtils.join(part, " ");
        }
        return StringUtils.join(sorts, ",");
    }

}

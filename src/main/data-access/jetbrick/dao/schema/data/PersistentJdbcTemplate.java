package jetbrick.dao.schema.data;

import java.util.*;
import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.id.*;
import jetbrick.dao.oam.*;
import org.apache.commons.lang.StringUtils;

/**
 * 负责通用的 PersistentData 对象的 DAO 操作 (无缓存)
 * 实际使用，请看 PersistentDAO
 */
public abstract class PersistentJdbcTemplate {
	protected final JdbcTemplate jdbc;
	protected final Dialect dialect;
	protected final SequenceIdProvider idProvider;
	protected final Map<Class<?>, String> sqls_cache_insert = new HashMap<Class<?>, String>();
	protected final Map<Class<?>, String> sqls_cache_update = new HashMap<Class<?>, String>();

	public PersistentJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		this.dialect = jdbc.getDialect();
		this.idProvider = new JdbcSequenceIdProvider(jdbc.getDataSource());
	}
	
	public Dialect getDialect() {
        return dialect;
    }

	public SequenceId createSequenceId(SchemaInfo<? extends PersistentData> schema) {
		return idProvider.create(schema.getTableName());
	}

	public int tableCreate(SchemaInfo<?> schema) {
		String sql = get_sql_create(schema);
		return jdbc.update(sql);
	}

	public int tableDelete(String tableName) {
		String sql = dialect.sql_table_drop(tableName);
		return jdbc.update(sql);
	}

	protected int jdbc_save(PersistentData data) {
		String sql = sqls_cache_insert.get(data.getClass());
		if (sql == null) {
			sql = get_sql_insert(data.getSchema());
			sqls_cache_insert.put(data.getClass(), sql);
		}
		return jdbc.update(sql, data.dao_insert_parameters());
	}

	protected int jdbc_update(PersistentData data) {
		String sql = sqls_cache_update.get(data.getClass());
		if (sql == null) {
			sql = get_sql_update(data.getSchema());
			sqls_cache_update.put(data.getClass(), sql);
		}
		return jdbc.update(sql, data.dao_update_parameters());
	}

	protected int jdbc_delete(PersistentData data) {
		String tableName = dialect.getIdentifier(data.getSchema().getTableName());
		String sql = "delete from " + tableName + " where id=?";
		return jdbc.update(sql, data.getId());
	}

	protected int jdbc_delete(Class<? extends PersistentData> schemaClass, Integer id) {
		SchemaInfo<?> schema = PersistentUtils.getSchema(schemaClass);
		String tableName = dialect.getIdentifier(schema.getTableName());
		String sql = "delete from " + tableName + " where id=?";
		return jdbc.update(sql, id);
	}

	protected <T extends PersistentData> T jdbc_get(Class<T> schemaClass, Integer id) {
		SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
		String tableName = dialect.getIdentifier(schema.getTableName());
		String sql = "select * from " + tableName + " where id=?";
		return jdbc.queryAsObject(schemaClass, sql, id);
	}

	protected <T extends PersistentData> T jdbc_get(Class<T> schemaClass, String name, Object value) {
		SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
		String tableName = dialect.getIdentifier(schema.getTableName());
		String columnName = dialect.getIdentifier(name);

		String sql = "select * from " + tableName + " where " + columnName + "=?";
		return jdbc.queryAsObject(schemaClass, sql, value);
	}

	public <T extends PersistentData> List<T> jdbc_get_some(Class<T> schemaClass, String name, Object value, String... sort) {
		SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
		String tableName = dialect.getIdentifier(schema.getTableName());
		String columnName = dialect.getIdentifier(name);

		String sql = "select * from " + tableName + " where " + columnName + "=?";
		if (sort != null && sort.length > 0) {
			sql = sql + " order by " + StringUtils.join(sort, ",");
		}
		return jdbc.queryAsList(schemaClass, sql, value);
	}

	protected <T extends PersistentData> List<T> jdbc_get_some(Class<T> schemaClass, Integer... ids) {
		SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
		String tableName = dialect.getIdentifier(schema.getTableName());
		String values = StringUtils.repeat("?", ",", ids.length);
		String sql = "select * from " + tableName + " where id in (" + values + ")";
		return jdbc.queryAsList(schemaClass, sql, (Object[]) ids);
	}

	protected <T extends PersistentData> List<T> jdbc_get_all(Class<T> schemaClass, String... sort) {
		SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
		String tableName = dialect.getIdentifier(schema.getTableName());
		String sql = "select * from " + tableName;
		if (sort != null && sort.length > 0) {
			sql = sql + " order by " + StringUtils.join(sort, ",");
		}
		return jdbc.queryAsList(schemaClass, sql);
	}

	protected String get_sql_insert(SchemaInfo<?> schema) {
		List<String> names = new ArrayList<String>();
		for (SchemaColumn c : schema.getColumns()) {
			names.add(dialect.getIdentifier(c.getColumnName()));
		}
		String sql = "insert into %s (%s) values (%s)";
		return String.format(sql, dialect.getIdentifier(schema.getTableName()), StringUtils.join(names, ","), StringUtils.repeat("?", ",", names.size()));
	}

	protected String get_sql_update(SchemaInfo<?> schema) {
		List<String> names = new ArrayList<String>();
		for (SchemaColumn c : schema.getColumns()) {
			if (!c.isPrimaryKey()) {
				names.add(dialect.getIdentifier(c.getColumnName()) + "=?");
			}
		}
		String sql = "update %s set %s where id=?";
		return String.format(sql, dialect.getIdentifier(schema.getTableName()), StringUtils.join(names, ","));
	}

	protected String get_sql_create(SchemaInfo<?> schema) {
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

	//------------------------------------------------------------
	public Integer queryAsInt(String sql, Object... parameters) {
		return jdbc.queryAsInt(sql, parameters);
	}

	public Long queryAsLong(String sql, Object... parameters) {
		return jdbc.queryAsLong(sql, parameters);
	}

	public String queryAsString(String sql, Object... parameters) {
		return jdbc.queryAsString(sql, parameters);
	}

	public Boolean queryAsBoolean(String sql, Object... parameters) {
		return jdbc.queryAsBoolean(sql, parameters);
	}

	public Date queryAsDate(String sql, Object... parameters) {
		return jdbc.queryAsDate(sql, parameters);
	}

	public Map<String, Object> queryAsMap(String sql, Object... parameters) {
		return jdbc.queryAsMap(sql, parameters);
	}

	public Object[] queryAsArray(String sql, Object... parameters) {
		return jdbc.queryAsArray(sql, parameters);
	}

	public int update(String sql, Object... parameters) {
		return jdbc.update(sql, parameters);
	}

	public JdbcTransaction transation() {
		return jdbc.transation();
	}

	public Object execute(ConnectionCallback callback) {
		return jdbc.execute(callback);
	}

	public boolean tableExist(String name) {
		return jdbc.tableExist(name);
	}
}

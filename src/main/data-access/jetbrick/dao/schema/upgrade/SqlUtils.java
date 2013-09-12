package jetbrick.dao.schema.upgrade;

import jetbrick.dao.dialect.Dialect;
import jetbrick.dao.schema.data.*;
import jetbrick.dao.schema.upgrade.model.DbColumn;

public class SqlUtils {
	private static final Dialect dialect = Entity.JDBC.getDialect();

	public static String sql_column_add(SchemaColumn c, SchemaColumn afterPosition) {
		String table_name = c.getSchema().getTableName();
		String column_name = dialect.getIdentifier(c.getColumnName());
		String column_type = dialect.asSqlType(c.getTypeName(), c.getTypeLength(), c.getTypeScale());
		String column_definition = String.format("%s %s", column_name, column_type);
		String column_position = (afterPosition == null) ? "first" : "after " + dialect.getIdentifier(afterPosition.getColumnName());

		return dialect.sql_column_add(table_name, column_definition, column_position);
	}

	public static String sql_column_modify(SchemaColumn c) {
		String table_name = c.getSchema().getTableName();
		String column_name = dialect.getIdentifier(c.getColumnName());
		String column_type = dialect.asSqlType(c.getTypeName(), c.getTypeLength(), c.getTypeScale());
		String nullable = c.isNullable() ? "null" : "";
		String column_definition = String.format("%s %s %s", column_name, column_type, nullable);

		return dialect.sql_column_modify(table_name, column_definition, null);
	}

	public static String sql_column_drop(SchemaInfo<?>	schema, DbColumn c) {
		return dialect.sql_column_drop(schema.getTableName(), c.getColumnName());
	}

	public static String sql_update_default_value(SchemaColumn c) {
		String table_name = dialect.getIdentifier(c.getSchema().getTableName());
		String column_name = dialect.getIdentifier(c.getColumnName());
		return String.format("update %s set %s = ?", table_name, column_name);
	}

}

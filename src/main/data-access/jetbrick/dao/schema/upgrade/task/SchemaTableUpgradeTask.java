package jetbrick.dao.schema.upgrade.task;

import java.sql.*;
import java.util.*;
import java.util.Date;
import jetbrick.commons.lang.DateUtils;
import jetbrick.dao.orm.ConnectionCallback;
import jetbrick.dao.schema.data.*;
import jetbrick.dao.schema.upgrade.*;
import jetbrick.dao.schema.upgrade.model.*;
import jetbrick.dao.schema.upgrade.model.SchemaTableDef.Action;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库表结构的自动升降级
 */
@SuppressWarnings("unchecked")
public class SchemaTableUpgradeTask extends UpgradeTask {
    private final Logger log = LoggerFactory.getLogger(SchemaTableUpgradeTask.class);
    private final SchemaHook hook;

    // 需要修改的schema任务队列
    private final List<SchemaTableDef> tableQueue = new ArrayList<SchemaTableDef>();
    private int sumAdded = 0;
    private int sumModified = 0;
    private int sumDeleted = 0;

    public SchemaTableUpgradeTask(UpgradeLogger fileLog, SchemaHook hook) {
        super(fileLog);
        this.hook = hook;
    }

    @Override
    public void initialize() {
        ensureCreate();

        // 读取数据库中存在的Schema
        Map<String, SchemaChecksum> db_checksum_map = ListOrderedMap.decorate(new CaseInsensitiveMap());
        List<SchemaChecksum> db_checksum_list = SchemaChecksum.DAO.loadSome("type", "TABLE");
        for (SchemaChecksum checksum : db_checksum_list) {
            db_checksum_map.put(checksum.getName(), checksum);
        }

        // 读取xml中定义的Schema
        Map<String, SchemaInfo<?>> xml_schema_map = ListOrderedMap.decorate(new CaseInsensitiveMap());
        List<SchemaInfo<? extends Entity>> xml_schema_list = EntityUtils.getSchemaList();
        for (SchemaInfo<?> schema : xml_schema_list) {
            xml_schema_map.put(schema.getTableName(), schema);
        }

        for (SchemaInfo<? extends Entity> schema : xml_schema_list) {
            SchemaChecksum checksum = db_checksum_map.get(schema.getTableName());
            if (checksum == null) {
                // added (id == null)
                checksum = SchemaChecksum.newInstance();
                checksum.setName(schema.getTableName());
                checksum.setType("TABLE");
                checksum.setChecksum(schema.getChecksum());
                checksum.setTimestamp(new Date());

                SchemaTableDef def = new SchemaTableDef();
                def.setAction(Action.CREATE);
                def.setChecksum(checksum);
                def.setSchema(schema);
                tableQueue.add(def);
            } else if (!StringUtils.equals(checksum.getChecksum(), schema.getChecksum())) {
                // updated
                checksum.setChecksum(schema.getChecksum());
                checksum.setTimestamp(new Date());

                SchemaTableDef def = new SchemaTableDef();
                def.setAction(Action.UPDATE);
                def.setChecksum(checksum);
                def.setSchema(schema);
                tableQueue.add(def);
            }
        }

        // deleted
        for (SchemaChecksum checksum : db_checksum_list) {
            if (!xml_schema_map.containsKey(checksum.getName())) {
                SchemaTableDef def = new SchemaTableDef();
                def.setAction(Action.DELETE);
                def.setChecksum(checksum);
                def.setSchema(null);
                tableQueue.add(def);
            }
        }

        db_checksum_list.clear();
        db_checksum_map.clear();
        xml_schema_list.clear();
        xml_schema_map.clear();
    }

    @Override
    public void destory() {
        tableQueue.clear();
    }

    @Override
    public boolean isRequired() {
        if (tableQueue.size() == 0) {
            log.info("Database schema upgrade is not required.");
        }
        return tableQueue.size() > 0;
    }

    @Override
    public void execute() {
        fileLog.println(">>>> Database Schema Table Upgrade checking ...");
        fileLog.println(">>>> date = %s", DateUtils.getNowStr());
        fileLog.println("");

        for (SchemaTableDef def : tableQueue) {
            String tableName = def.getChecksum().getName();
            if (def.getAction() == Action.CREATE) {
                if (dao.tableExist(tableName)) {
                    log.warn(String.format("Table %s exists, create skipped, will be updated.", tableName));
                    tableModify(def.getSchema(), def.getChecksum());
                } else {
                    tableCreate(def.getSchema());
                }
                // add table checksum
                def.getChecksum().save();
            } else if (def.getAction() == Action.UPDATE) {
                if (dao.tableExist(tableName)) {
                    log.warn(String.format("Table %s does not exist, will be created.", tableName));
                    tableModify(def.getSchema(), def.getChecksum());
                } else {
                    tableCreate(def.getSchema());
                }
                // update table checksum
                def.getChecksum().update();
            } else if (def.getAction() == Action.DELETE) {
                tableDelete(def.getChecksum());
                // remove table checksum
                def.getChecksum().delete();
            }
        }

        fileLog.println(">>>> Database Schema Upgrade completed.\n");
        fileLog.println(">>>> Total tables: %d created, %d modified, %d deleted.\n", sumAdded, sumModified, sumDeleted);
        fileLog.println("");
    }

    private void tableCreate(SchemaInfo<?> schema) {
        EntityUtils.getEntityDaoHelper(schema.getTableClass()).tableCreate();
        sumAdded++;

        fileLog.println("Table created: " + schema.getTableName());

        if (hook != null) hook.whenTableCreated(schema);
    }

    private void tableDelete(SchemaChecksum table) {
        String sql = dialect.sql_table_drop(table.getName());
        executeSQLWithFileLog(sql);
        sumDeleted++;

        fileLog.println("Table deleted: " + table.getName());
    }

    private void tableModify(SchemaInfo<?> schema, SchemaChecksum table) {
        updateColumns(schema, table);
        sumModified++;
    }

    private void updateColumns(SchemaInfo<?> schema, SchemaChecksum checksum) {
        Map<String, DbColumn> db_column_map = getColumnsFromDatabase(checksum);

        SchemaColumn lastColumn = null;
        for (SchemaColumn sc : schema.getColumns()) {
            DbColumn dc = db_column_map.get(sc.getColumnName());

            if (dc == null) {
                // add column
                String sql = UpgradeSqlUtils.sql_column_add(dialect, sc, lastColumn);
                executeSQLWithFileLog(sql);

                // set default value for new added column
                if (sc.getDefaultValue() != null) {
                    sql = UpgradeSqlUtils.sql_update_default_value(dialect, sc);
                    executeSQLWithFileLog(sql, sc.getDefaultValue());
                }
                if (hook != null) hook.whenColumnCreated(sc);

            } else {
                if (isColumnChanged(sc, dc)) {
                    String sql = UpgradeSqlUtils.sql_column_modify(dialect, sc);
                    executeSQLWithFileLog(sql);
                }
            }
            lastColumn = sc;
        }

        for (DbColumn dc : db_column_map.values()) {
            if (db_column_map.get(dc.getColumnName()) == null) {
                // drop column
                String sql = UpgradeSqlUtils.sql_column_drop(dialect, schema, dc);
                executeSQLWithFileLog(sql);
            }
        }
    }

    public Map<String, DbColumn> getColumnsFromDatabase(final SchemaChecksum table) {
        final List<DbColumn> columnList = new ArrayList<DbColumn>();
        dao.execute(new ConnectionCallback() {
            @Override
            public void execute(Connection conn) throws SQLException {
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet rs = metaData.getColumns(null, null, table.getName().toUpperCase(), null);
                while (rs.next()) {
                    DbColumn c = new DbColumn();
                    c.setColumnName(rs.getString("COLUMN_NAME"));
                    c.setTypeName(rs.getString("TYPE_NAME"));
                    if (dialect.supportsColumnLength(c.getTypeName())) {
                        c.setTypeLength(rs.getInt("COLUMN_SIZE"));
                    }
                    if (dialect.supportsColumnScale(c.getTypeName())) {
                        c.setTypeScale(rs.getInt("DECIMAL_DIGITS"));
                    }
                    c.setNullable(rs.getBoolean("NULLABLE"));
                    c.setDefaultValue(rs.getObject("COLUMN_DEF"));
                    columnList.add(c);
                }
            }
        });

        Map<String, DbColumn> columns = ListOrderedMap.decorate(new CaseInsensitiveMap());
        for (final DbColumn dc : columnList) {
            columns.put(dc.getColumnName(), dc);
        }
        return columns;
    }

    private boolean isColumnChanged(SchemaColumn sc, DbColumn dc) {
        String column_type = dialect.asSqlType(sc.getTypeName(), sc.getTypeLength(), sc.getTypeScale());

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(sc.getColumnName().toUpperCase(), dc.getColumnName().toUpperCase());
        builder.append(column_type.toUpperCase(), dc.asSqlType().toUpperCase());
        builder.append(sc.isNullable(), dc.isNullable());
        boolean changed = !builder.isEquals();

        if (changed) {
            String message = "changed column: " + sc.getColumnName();
            message += ", type: " + dc.asSqlType().toUpperCase() + " -> " + column_type;
            message += ", nullable: " + dc.isNullable() + " -> " + sc.isNullable();
            log.warn(message);
        }
        return changed;
    }

    private void ensureCreate() {
        if (!SchemaChecksum.DAO.tableExist()) {
            SchemaChecksum.DAO.tableCreate();
        }
    }
}

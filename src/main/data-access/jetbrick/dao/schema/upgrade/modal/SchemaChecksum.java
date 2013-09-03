package jetbrick.dao.schema.upgrade.modal;

import jetbrick.dao.schema.data.*;
import jetbrick.dao.utils.DbUtils;
import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("serial")
public class SchemaChecksum extends PersistentData {

	//------ field list ---------------------------------------
	protected String name;
	protected String type;
	protected String checksum;
	protected java.util.Date timestamp;

	//------ schema -----------------------------------------
	public static final SchemaInfo<SchemaChecksum> SCHEMA;
	public static final SchemaColumn SC_ID;
	public static final SchemaColumn SC_NAME;
	public static final SchemaColumn SC_TYPE;
	public static final SchemaColumn SC_CHECKSUM;
	public static final SchemaColumn SC_TIMESTAMP;

	@Override
	public SchemaInfo<SchemaChecksum> getSchema() {
		return SCHEMA;
	}

	static {
		SchemaInfoImpl<SchemaChecksum> schema = new SchemaInfoImpl<SchemaChecksum>();
		SCHEMA = schema;

		schema.setTableName("_schema_checksum");
		schema.setTableClass(SchemaChecksum.class);
		schema.setCacheSupport(false);
		schema.setCacheMaxSize(0);
		schema.setCacheMaxLiveSeconds(0);
		schema.setCacheMaxIdleSeconds(0);
		schema.setDisplayName("数据库表总揽");
		schema.setDescription(null);
		schema.setChecksum(null);
		schema.setTimestamp(null);

		// column list
		SC_ID = schema.addColumn("id", Integer.class, "id", "long", null, null, false, null, "ID", null, true, null, true);
		SC_NAME = schema.addColumn("name", String.class, "name", "varchar", 50, null, false, null, "名称", null, false, null, true);
		SC_TYPE = schema.addColumn("type", String.class, "type", "varchar", 20, null, false, null, "类型", null, false, null, true);
		SC_CHECKSUM = schema.addColumn("checksum", String.class, "checksum", "char", 32, null, false, null, "校验码", null, false, null, true);
		SC_TIMESTAMP = schema.addColumn("timestamp", java.util.Date.class, "timestamp", "datetime", null, null, false, null, "时间戳", null, false, null, true);

		// column validator
		schema.addDefaultValidators();
	}

	//------- id ------------------------------------------------------
	private static final jetbrick.dao.id.SequenceId sequenceId = DbUtils.dao().createSequenceId(SCHEMA);

	@Override
    public Integer generateId() {
        if (id == null) {
            id = sequenceId.nextVal();
        }
        return id;
    }
    
	//------ getter / setter -----------------------------------------
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public java.util.Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(java.util.Date timestamp) {
		this.timestamp = timestamp;
	}

	//------ dao parameters -----------------------------------------
	@Override
	public Object[] dao_insert_parameters() {
		return new Object[] { id, name, type, checksum, timestamp };
	}

	@Override
	public Object[] dao_update_parameters() {
		return new Object[] { name, type, checksum, timestamp, id };
	}

	//------ validate -----------------------------------------
	@Override
	public void validate() {
		validate(SC_NAME, name);
		validate(SC_TYPE, type);
		validate(SC_CHECKSUM, checksum);
		validate(SC_TIMESTAMP, timestamp);
	}

	//------ cache -----------------------------------------
	@SuppressWarnings("unchecked")
	public static final PersistentCache<SchemaChecksum> CACHE = PersistentCache.NO_CACHE;

	@Override
	public PersistentCache<SchemaChecksum> getCache() {
		return CACHE;
	}

	//------ instance -----------------------------------------
	public static SchemaChecksum newInstance() {
		SchemaChecksum data = new SchemaChecksum();
		data.makeDefaults();
		return data;
	}

	@Override
	public void makeDefaults() {
	}

	//------ clone -----------------------------------------
	@Override
	public SchemaChecksum clone() {
		return cloneFields(this, new SchemaChecksum());
	}

	public void copy(SchemaChecksum copy) {
		cloneFields(copy, this);
	}

	private SchemaChecksum cloneFields(SchemaChecksum from, SchemaChecksum to) {
		to.id = from.id;
		to.name = from.name;
		to.type = from.type;
		to.checksum = from.checksum;
		to.timestamp = from.timestamp;
		return to;
	}

	//------ json -----------------------------------------
	@Override
	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("name", name);
		json.put("type", type);
		json.put("checksum", checksum);
		json.put("timestamp", timestamp);
		return json;
	}

	//------ end -----------------------------------------
}

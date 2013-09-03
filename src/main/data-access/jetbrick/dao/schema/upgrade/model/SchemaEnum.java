package jetbrick.dao.schema.upgrade.model;

import jetbrick.dao.schema.data.*;
import jetbrick.dao.utils.DbUtils;
import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("serial")
public class SchemaEnum extends PersistentData {

	//------ field list ---------------------------------------
	protected Integer pid;
	protected String name;
	protected String defineName;
	protected String description;

	//------ schema -----------------------------------------
	public static final SchemaInfo<SchemaEnum> SCHEMA;
	public static final SchemaColumn SC_ID;
	public static final SchemaColumn SC_PID;
	public static final SchemaColumn SC_NAME;
	public static final SchemaColumn SC_DEFINE_NAME;
	public static final SchemaColumn SC_DESCRIPTION;

	@Override
	public SchemaInfo<SchemaEnum> getSchema() {
		return SCHEMA;
	}

	static {
		SchemaInfoImpl<SchemaEnum> schema = new SchemaInfoImpl<SchemaEnum>();
		SCHEMA = schema;

		schema.setTableName("_schema_enum");
		schema.setTableClass(SchemaEnum.class);
		schema.setCacheSupport(false);
		schema.setCacheMaxSize(0);
		schema.setCacheMaxLiveSeconds(0);
		schema.setCacheMaxIdleSeconds(0);
		schema.setDisplayName("全局枚举变量");
		schema.setDescription(null);
		schema.setChecksum(null);
		schema.setTimestamp(null);

		// column list
		SC_ID = schema.addColumn("id", Integer.class, "id", "long", null, null, false, null, "ID", null, true, null, true);
		SC_PID = schema.addColumn("pid", String.class, "pid", "int", null, null, false, null, "分组ID", null, false, null, true);
		SC_NAME = schema.addColumn("name", String.class, "name", "varchar", 50, null, false, null, "名称", null, false, null, true);
		SC_DEFINE_NAME = schema.addColumn("defineName", String.class, "define_name", "varchar", 50, null, true, null, "变量名", null, false, null, true);
		SC_DESCRIPTION = schema.addColumn("description", String.class, "description", "varchar", 250, null, true, null, "描述", null, false, null, true);

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
	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefineName() {
		return defineName;
	}

	public void setDefineName(String defineName) {
		this.defineName = defineName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	//------ dao parameters -----------------------------------------
	@Override
	public Object[] dao_insert_parameters() {
		return new Object[] { id, pid, name, defineName, description };
	}

	@Override
	public Object[] dao_update_parameters() {
		return new Object[] { pid, name, defineName, description, id };
	}

	//------ validate -----------------------------------------
	@Override
	public void validate() {
		validate(SC_PID, pid);
		validate(SC_NAME, name);
		validate(SC_DEFINE_NAME, defineName);
		validate(SC_DESCRIPTION, description);
	}

	//------ cache -----------------------------------------
	public static final String CACHE_NAME = SchemaEnum.class.getName();
	public static final PersistentCache<SchemaChecksum> CACHE = new PersistentCache<SchemaChecksum>(CACHE_NAME, 10000, true, 0, 0);

	@Override
	public PersistentCache<SchemaChecksum> getCache() {
		return CACHE;
	}

	//------ instance -----------------------------------------
	public static SchemaEnum newInstance() {
		SchemaEnum data = new SchemaEnum();
		data.makeDefaults();
		return data;
	}

	@Override
	public void makeDefaults() {
	}

	//------ clone -----------------------------------------
	@Override
	public SchemaEnum clone() {
		return cloneFields(this, new SchemaEnum());
	}

	public void copy(SchemaEnum copy) {
		cloneFields(copy, this);
	}

	private SchemaEnum cloneFields(SchemaEnum from, SchemaEnum to) {
		to.id = from.id;
		to.pid = from.pid;
		to.name = from.name;
		to.defineName = from.defineName;
		to.description = from.description;
		return to;
	}

	//------ json -----------------------------------------
	@Override
	public JSONObject toJSONObject() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("pid", pid);
		json.put("name", name);
		json.put("defineName", defineName);
		json.put("description", description);
		return json;
	}

	//------ end -----------------------------------------
}

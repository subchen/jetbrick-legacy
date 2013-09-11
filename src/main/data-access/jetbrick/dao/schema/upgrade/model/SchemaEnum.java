package jetbrick.dao.schema.upgrade.model;

import jetbrick.dao.oam.*;
import jetbrick.dao.schema.data.*;
import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("serial")
public class SchemaEnum extends Entity {

    //------ field list ---------------------------------------
    protected Integer pid;  // (pid)
    protected String name;  // (name)
    protected String defineName;  // (define_name)
    protected String description;  // (description)

    //------ schema -------------------------------------------
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
        
        schema.setTableName("schema_enum");
        schema.setTableClass(SchemaEnum.class);
        schema.setCacheSupport(false);
        schema.setCacheMaxSize(0);
        schema.setCacheMaxLiveSeconds(0);
        schema.setCacheMaxIdleSeconds(0);
        schema.setDisplayName(null);
        schema.setDescription("全局枚举变量");
        schema.setChecksum("e6be831660e3dffc8f5c9b9a54bc5378");
        schema.setTimestamp("2013-09-12 00:42:46");

        // column list
        SC_ID = schema.addColumn("id", Integer.class, "id", "id", null, null, false, null, "ID", null, true, null, true);
        SC_PID = schema.addColumn("pid", Integer.class, "pid", "int", null, null, false, null, null, null, false, null, true);
        SC_NAME = schema.addColumn("name", String.class, "name", "varchar", 50, null, false, null, null, null, false, null, true);
        SC_DEFINE_NAME = schema.addColumn("defineName", String.class, "define_name", "varchar", 50, null, true, null, null, null, false, null, true);
        SC_DESCRIPTION = schema.addColumn("description", String.class, "description", "char", 32, null, true, null, null, null, false, null, true);

        // column validator
        schema.addDefaultValidators();
    }

    //------- id ----------------------------------------------
    private static final jetbrick.dao.id.SequenceId sequence_id = EntityUtils.createSequenceId(SchemaEnum.class);

    @Override
    public Integer generateId() {
        if (id == null) {
            id = sequence_id.nextVal();
        }
        return id;
    }
    
    //------ getter / setter ----------------------------------
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

    //------ one-to-many --------------------------------------

    //------ cache --------------------------------------------
    @SuppressWarnings("unchecked")
    public static final EntityCache CACHE = EntityCache.NO_CACHE;

    @Override
    public EntityCache<SchemaEnum> getCache() {
        return CACHE;
    }

    //------ dao -----------------------------------
    public static final EntityDaoHelper DAO_HELPER = EntityUtils.DAO_HELPER;
    
    @Override
    public EntityDaoHelper getEntityDaoHelper() {
        return DAO_HELPER;
    }
    
    @Override
    public Object[] dao_insert_parameters() {
        return new Object[] { id, pid, name, defineName, description };
    }

    @Override
    public Object[] dao_update_parameters() {
        return new Object[] { pid, name, defineName, description, id };
    }

    public static final RowMapper ROW_MAPPER = new RowMapper<SchemaEnum>() {
        public SchemaEnum handle(java.sql.ResultSet rs) throws java.sql.SQLException {
            SchemaEnum info = new SchemaEnum();
            info.id = rs.getInt("id");
            info.pid = rs.getInt("pid");
            info.name = rs.getString("name");
            info.defineName = rs.getString("defineName");
            info.description = rs.getString("description");
            return info;
        }
    };

    //------ validate -----------------------------------------
    @Override
    public void validate() {
        validate(SC_PID, pid);
        validate(SC_NAME, name);
        validate(SC_DEFINE_NAME, defineName);
        validate(SC_DESCRIPTION, description);
    }

    //------ hashcode / equals --------------------------------
    @Override
    public int hashCode() {
        int result = 17;
        if (id != null) result = 31 * result + id.hashCode();
        if (pid != null) result = 31 * result + pid.hashCode();
        if (name != null) result = 31 * result + name.hashCode();
        if (defineName != null) result = 31 * result + defineName.hashCode();
        if (description != null) result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        SchemaEnum o = (SchemaEnum) obj;
        //@formatter:off
        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(id, o.id) 
            .append(pid, o.pid) 
            .append(name, o.name) 
            .append(defineName, o.defineName) 
            .append(description, o.description) 
            .isEquals();
        //@formatter:on
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

    //------ clone --------------------------------------------
    @Override
    public SchemaEnum clone() {
        return cloneFields(this, new SchemaEnum());
    }

    public void copy(SchemaEnum from) {
        cloneFields(from, this);
    }

    private SchemaEnum cloneFields(SchemaEnum from, SchemaEnum to) {
        to.id = from.id;
        to.pid = from.pid;
        to.name = from.name;
        to.defineName = from.defineName;
        to.description = from.description;
        return to;
    }

    //------ json ---------------------------------------------
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

    //------ end ----------------------------------------------
}


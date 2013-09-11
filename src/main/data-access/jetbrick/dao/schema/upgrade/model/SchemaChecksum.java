package jetbrick.dao.schema.upgrade.model;

import jetbrick.dao.oam.*;
import jetbrick.dao.schema.data.*;
import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("serial")
public class SchemaChecksum extends Entity {

    //------ field list ---------------------------------------
    protected String name;  // (name)
    protected String type;  // (type)
    protected String checksum;  // (checksum)
    protected java.util.Date timestamp;  // (timestamp)

    //------ schema -------------------------------------------
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
        schema.setDisplayName(null);
        schema.setDescription("数据库表总揽");
        schema.setChecksum("07e905b587a3398f70e88b8185ac63f4");
        schema.setTimestamp("2013-09-12 00:41:10");

        // column list
        SC_ID = schema.addColumn("id", Integer.class, "id", "id", null, null, false, null, "ID", null, true, null, true);
        SC_NAME = schema.addColumn("name", String.class, "name", "varchar", 50, null, false, null, null, null, false, null, true);
        SC_TYPE = schema.addColumn("type", String.class, "type", "varchar", 50, null, false, null, null, null, false, null, true);
        SC_CHECKSUM = schema.addColumn("checksum", String.class, "checksum", "char", 32, null, false, null, null, null, false, null, true);
        SC_TIMESTAMP = schema.addColumn("timestamp", java.util.Date.class, "timestamp", "datetime", null, null, false, null, null, null, false, null, true);

        // column validator
        schema.addDefaultValidators();
    }

    //------- id ----------------------------------------------
    private static final jetbrick.dao.id.SequenceId sequence_id = EntityUtils.createSequenceId(SchemaChecksum.class);

    @Override
    public Integer generateId() {
        if (id == null) {
            id = sequence_id.nextVal();
        }
        return id;
    }
    
    //------ getter / setter ----------------------------------
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

    //------ one-to-many --------------------------------------

    //------ cache --------------------------------------------
    @SuppressWarnings("unchecked")
    public static final EntityCache CACHE = EntityCache.NO_CACHE;

    @Override
    public EntityCache<SchemaChecksum> getCache() {
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
        return new Object[] { id, name, type, checksum, timestamp };
    }

    @Override
    public Object[] dao_update_parameters() {
        return new Object[] { name, type, checksum, timestamp, id };
    }

    public static final RowMapper ROW_MAPPER = new RowMapper<SchemaChecksum>() {
        public SchemaChecksum handle(java.sql.ResultSet rs) throws java.sql.SQLException {
            SchemaChecksum info = new SchemaChecksum();
            info.id = rs.getInt("id");
            info.name = rs.getString("name");
            info.type = rs.getString("type");
            info.checksum = rs.getString("checksum");
            info.timestamp = rs.getTimestamp("timestamp");
            return info;
        }
    };

    //------ validate -----------------------------------------
    @Override
    public void validate() {
        validate(SC_NAME, name);
        validate(SC_TYPE, type);
        validate(SC_CHECKSUM, checksum);
        validate(SC_TIMESTAMP, timestamp);
    }

    //------ hashcode / equals --------------------------------
    @Override
    public int hashCode() {
        int result = 17;
        if (id != null) result = 31 * result + id.hashCode();
        if (name != null) result = 31 * result + name.hashCode();
        if (type != null) result = 31 * result + type.hashCode();
        if (checksum != null) result = 31 * result + checksum.hashCode();
        if (timestamp != null) result = 31 * result + timestamp.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        SchemaChecksum o = (SchemaChecksum) obj;
        //@formatter:off
        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(id, o.id) 
            .append(name, o.name) 
            .append(type, o.type) 
            .append(checksum, o.checksum) 
            .append(timestamp, o.timestamp) 
            .isEquals();
        //@formatter:on
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

    //------ clone --------------------------------------------
    @Override
    public SchemaChecksum clone() {
        return cloneFields(this, new SchemaChecksum());
    }

    public void copy(SchemaChecksum from) {
        cloneFields(from, this);
    }

    private SchemaChecksum cloneFields(SchemaChecksum from, SchemaChecksum to) {
        to.id = from.id;
        to.name = from.name;
        to.type = from.type;
        to.checksum = from.checksum;
        to.timestamp = from.timestamp;
        return to;
    }

    //------ json ---------------------------------------------
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

    //------ end ----------------------------------------------
}


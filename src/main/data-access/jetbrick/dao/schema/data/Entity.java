package jetbrick.dao.schema.data;

import java.io.Serializable;
import jetbrick.dao.schema.validator.Validator;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

/**
 * 数据库表的对象的基类
 */
@SuppressWarnings("serial")
public abstract class Entity implements Serializable, Cloneable, JSONAware {

    //------ id -----------------------------------------
    protected Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    //生成并返回ID
    public abstract Integer generateId();

    //------ schema -----------------------------------------
    public abstract SchemaInfo<? extends Entity> getSchema();

    //------ cache -----------------------------------------
    public abstract EntityCache<? extends Entity> getCache();

    //------ dao ---------------------------------
    public abstract EntityDaoHelper getEntityDaoHelper();

    public int save() {
        return getEntityDaoHelper().save(this);
    }

    public int update() {
        return getEntityDaoHelper().update(this);
    }

    public int saveOrUpdate() {
        return getEntityDaoHelper().saveOrUpdate(this);
    }

    public int delete() {
        return getEntityDaoHelper().delete(this);
    }

    public abstract Object[] dao_insert_parameters();

    public abstract Object[] dao_update_parameters();

    //------ validate -----------------------------------------
    public abstract void validate();

    protected void validate(SchemaColumn column, Object value) {
        for (Validator v : column.getValidators()) {
            v.validate(column.getFieldName(), value);
        }
    }

    //------ default value -----------------------------------------
    public abstract void makeDefaults();

    //------ json -----------------------------------------
    public abstract JSONObject toJSONObject();

    @Override
    public String toJSONString() {
        return toJSONObject().toString();
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }

    //------ end -----------------------------------------
}

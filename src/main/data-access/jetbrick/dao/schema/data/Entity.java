package jetbrick.dao.schema.data;

import java.io.Serializable;
import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

/**
 * 数据库表的对象的基类
 */
@SuppressWarnings("serial")
public abstract class Entity implements Serializable, Cloneable, JSONAware {

    public static final Serializable[] EMPTY_ID_ARRAY = new Serializable[0];

    //------ id -----------------------------------------
    protected Serializable id;

    public Serializable getId() {
        return id;
    }

    //生成并返回ID
    public abstract Serializable generateId();

    //------ schema -----------------------------------------
    public abstract SchemaInfo<? extends Entity> schema();

    //------ cache -----------------------------------------
    public abstract EntityCache<? extends Entity> cache();

    //------ dao ---------------------------------
    public abstract EntityDaoHelper dao();

    public int save() {
        return dao().save(this);
    }

    public int update() {
        return dao().update(this);
    }

    public int saveOrUpdate() {
        return dao().saveOrUpdate(this);
    }

    public int delete() {
        return dao().delete(this);
    }

    public abstract Object[] dao_insert_parameters();

    public abstract Object[] dao_update_parameters();

    //------ validate -----------------------------------------
    public abstract void validate();

    protected void validate(SchemaColumn column, Object value) {
        if (value == null || "".equals(value)) {
            if (column.isNullable()) return;
            throw new ValidatorException("%s must be not empty.", column.getDisplayName());
        }
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

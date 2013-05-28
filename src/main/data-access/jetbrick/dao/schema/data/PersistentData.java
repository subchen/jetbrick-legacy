package jetbrick.dao.schema.data;

import java.io.Serializable;
import jetbrick.dao.schema.validator.Validator;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

/**
 * 数据库表的对象的基类
 */
@SuppressWarnings("serial")
public abstract class PersistentData implements Serializable, Cloneable, JSONAware {

	//------ id -----------------------------------------
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	//生成并返回ID
	public abstract Long generateId();

	//------ schema -----------------------------------------
	public abstract SchemaInfo<? extends PersistentData> getSchema();

	//------ cache -----------------------------------------
	public abstract PersistentCache<? extends PersistentData> getCache();

	//------ validate -----------------------------------------
	public abstract void validate();

	protected void validate(SchemaColumn column, Object value) {
		for (Validator v : column.getValidators()) {
			v.validate(column.getFieldName(), value);
		}
	}

	//------ dao parameters ---------------------------------
	public abstract Object[] dao_insert_parameters();

	public abstract Object[] dao_update_parameters();

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

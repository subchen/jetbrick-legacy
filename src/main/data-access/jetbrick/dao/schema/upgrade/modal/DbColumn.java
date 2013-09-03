package jetbrick.dao.schema.upgrade.modal;

import jetbrick.dao.dialect.SqlType;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DbColumn {
	protected String columnName;
	protected String typeName;
	protected Integer typeLength;
	protected Integer typeScale;
	protected boolean nullable;
	protected Object defaultValue;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getTypeLength() {
		return typeLength;
	}

	public void setTypeLength(Integer typeLength) {
		this.typeLength = typeLength;
	}

	public Integer getTypeScale() {
		return typeScale;
	}

	public void setTypeScale(Integer typeScale) {
		this.typeScale = typeScale;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String asSqlType() {
		return SqlType.newInstance(typeName, typeLength, typeScale).toString();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

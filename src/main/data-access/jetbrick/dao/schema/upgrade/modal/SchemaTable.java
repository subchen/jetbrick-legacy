package jetbrick.dao.schema.upgrade.modal;

import jetbrick.dao.schema.data.PersistentData;
import jetbrick.dao.schema.data.SchemaInfo;

public class SchemaTable {
	public enum Action {
		CREATE, UPDATE, DELETE
	}

	private Action action;
	private SchemaInfo<? extends PersistentData> schema;
	private SchemaChecksum checksum;

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public SchemaInfo<? extends PersistentData> getSchema() {
		return schema;
	}

	public void setSchema(SchemaInfo<? extends PersistentData> schema) {
		this.schema = schema;
	}

	public SchemaChecksum getChecksum() {
		return checksum;
	}

	public void setChecksum(SchemaChecksum checksum) {
		this.checksum = checksum;
	}

}

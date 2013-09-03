package jetbrick.dao.schema.upgrade.modal;

import jetbrick.dao.schema.data.PersistentData;

public class SchemaBulkFile {
	private String fileName;
	private Class<? extends PersistentData> tableClass;
	private String checksum;
	private SchemaChecksum info;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Class<? extends PersistentData> getTableClass() {
		return tableClass;
	}

	public void setTableClass(Class<? extends PersistentData> tableClass) {
		this.tableClass = tableClass;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public SchemaChecksum getInfo() {
		return info;
	}

	public void setInfo(SchemaChecksum info) {
		this.info = info;
	}
}

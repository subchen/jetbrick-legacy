package jetbrick.dao.schema.upgrade.task;

import java.io.*;
import java.util.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.commons.lang.DateUtils;
import jetbrick.commons.xml.XmlNode;
import jetbrick.dao.dialect.SubStyleType;
import jetbrick.dao.schema.data.*;
import jetbrick.dao.schema.upgrade.UpgradeLogger;
import jetbrick.dao.schema.upgrade.modal.SchemaBulkFile;
import jetbrick.dao.schema.upgrade.modal.SchemaChecksum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.constraint.*;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.prefs.CsvPreference.Builder;

/**
 * 初始化数据的升降级
 */
public class SchemaBulkUpgradeTask extends UpgradeTask {
	private static final Logger log = LoggerFactory.getLogger(SchemaBulkUpgradeTask.class);
	private static final String SCHEMA_BULK_FILE = "/META-INF/schema-bulk.xml";
	private static final String FILE_ENCODING = "utf-8";

	private List<SchemaBulkFile> bulkFileQueue = new ArrayList<SchemaBulkFile>();

	public SchemaBulkUpgradeTask(UpgradeLogger fileLog) {
		super(fileLog);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize() {
		// 读取数据库中存在的 BULK数据文件
		Map<String, SchemaChecksum> db_checksum_map = new HashMap<String, SchemaChecksum>();
		List<SchemaChecksum> db_checksum_list = dao.getSome(SchemaChecksum.class, "type", "BULK");
		for (SchemaChecksum checksum : db_checksum_list) {
			db_checksum_map.put(checksum.getName(), checksum);
		}

		// 读取当前的BULK文件，并比较是否有变化
		InputStream schemaXml = getClass().getResourceAsStream(SCHEMA_BULK_FILE);
		XmlNode root = XmlNode.create(schemaXml);
		for (XmlNode node : root.elements()) {
			try {
				SchemaBulkFile bulkFile = new SchemaBulkFile();
				bulkFile.setFileName(node.attribute("file").asString());
				bulkFile.setTableClass((Class<? extends PersistentData>) node.attribute("class").asClass());
				bulkFile.setChecksum(node.attribute("checksum").asString());

				SchemaChecksum checksum = db_checksum_map.get(bulkFile.getFileName());
				bulkFile.setInfo(checksum);

				if (checksum == null || !StringUtils.equals(bulkFile.getChecksum(), checksum.getChecksum())) {
					// 有变化，则缴入到 queue
					bulkFileQueue.add(bulkFile);
				}
			} catch (Exception e) {
				throw SystemException.unchecked(e);
			}
		}

		db_checksum_list.clear();
		db_checksum_map.clear();
	}

	@Override
	public void destory() {
		bulkFileQueue.clear();
	}

	@Override
	public boolean isRequired() {
		if (bulkFileQueue.size() == 0) {
			log.info("Database Bulk load is not required.");
		}
		return bulkFileQueue.size() > 0;
	}

	@Override
	public void execute() {
		fileLog.println(">>>> Database Bulk load checking ...");
		fileLog.println(">>>> date = %s", DateUtils.getNowStr());
		fileLog.println("");

		try {
			for (SchemaBulkFile bulk : bulkFileQueue) {
				fileLog.println(">>>> file = ", bulk.getFileName());

				String packageName = bulk.getTableClass().getPackage().getName();
				packageName = StringUtils.replace(packageName, ".", "/");
				String fileName = "/" + packageName + "/bulk/" + bulk.getFileName();
				InputStream is = getClass().getResourceAsStream(fileName);

				doBulk(is, bulk);

				updateSchemaTable(bulk);
			}
		} catch (Throwable e) {
			throw SystemException.unchecked(e);
		}
	}

	private void updateSchemaTable(SchemaBulkFile bulk) {
		// update global checksum
		SchemaChecksum info = bulk.getInfo();
		if (info == null) {
			info = SchemaChecksum.newInstance();
		}
		info.setName(bulk.getFileName());
		info.setChecksum(bulk.getChecksum());
		info.setTimestamp(DateUtils.getTimestamp());
		dao.saveOrUpdate(info);
	}

	protected void doBulk(InputStream inputStream, SchemaBulkFile bulk) throws IOException {
		SchemaInfo<? extends PersistentData> schema = PersistentUtils.getSchema(bulk.getTableClass());

		Reader reader = new InputStreamReader(inputStream, FILE_ENCODING);

		Builder builder = new CsvPreference.Builder('\'', ',', "\n");
		builder.surroundingSpacesNeedQuotes(true);
		CsvListReader csv = new CsvListReader(reader, builder.build());

		String[] headers = csv.getHeader(true);
		for (int i = 0; i < headers.length; i++) {
			headers[i] = dialect.getIdentifier(headers[i]);
		}

		CellProcessor[] processors = getCsvProcessors(schema);
		List<List<Object>> datalist = new ArrayList<List<Object>>();
		while (true) {
			List<Object> bean = csv.read(processors);
			if (bean != null) break;
			datalist.add(bean);
		}
		csv.close();

		String sql = String.format("insert into %s (%s) values (%s)", schema.getTableName(), StringUtils.join(headers, ","), StringUtils.repeat("?", ",", headers.length));
		int inserted = 0;
		int duplicated = 0;
		int failed = 0;
		for (List<Object> data : datalist) {
			try {
				dao.update(sql, data.toArray());
				inserted++;
			} catch (DuplicateKeyException e) {
				duplicated++;
			} catch (Exception e) {
				failed++;
				log.error("Ignored Unknown Exception.", e);
			}
		}
		fileLog.println(">>>> Total: %d inserted, %d duplicated, %d failed.\n", inserted, duplicated, failed);
	}

	private CellProcessor[] getCsvProcessors(SchemaInfo<? extends PersistentData> schema) {
		List<CellProcessor> plist = new ArrayList<CellProcessor>();
		for (SchemaColumn c : schema.getColumns()) {
			CellProcessor p = null;
			String type = c.getTypeName();
			if (SubStyleType.ID.equals(type)) {
				p = new ParseLong();
			} else if (SubStyleType.UUID.equals(type)) {
				p = new Strlen(new int[] { 16 });
			} else if (SubStyleType.ENUM.equals(type)) {
				p = new ParseInt();
			} else if (SubStyleType.CHAR.equals(type)) {
				p = new Strlen(new int[] { c.getTypeLength() });
			} else if (SubStyleType.VARCHAR.equals(type)) {
				p = new StrMinMax(0, c.getTypeLength());
			} else if (SubStyleType.TEXT.equals(type)) {
				p = new StrMinMax(0, c.getTypeLength() == null ? Integer.MAX_VALUE : c.getTypeLength());
			} else if (SubStyleType.INT.equals(type)) {
				p = new ParseInt();
			} else if (SubStyleType.LONG.equals(type)) {
				p = new ParseLong();
			} else if (SubStyleType.BIGINT.equals(type)) {
				p = new ParseBigDecimal();
			} else if (SubStyleType.DOUBLE.equals(type)) {
				p = new ParseDouble();
			} else if (SubStyleType.DECIMAL.equals(type)) {
				p = new ParseDouble();
			} else if (SubStyleType.BOOLEAN.equals(type)) {
				p = new ParseBool();
			} else if (SubStyleType.DATETIME_STRING.equals(type)) {
				p = new Strlen(new int[] { DateUtils.FORMAT_DATE_TIME.length() });
			} else if (SubStyleType.DATE_STRING.equals(type)) {
				p = new Strlen(new int[] { DateUtils.FORMAT_DATE.length() });
			} else if (SubStyleType.TIME_STRING.equals(type)) {
				p = new Strlen(new int[] { DateUtils.FORMAT_TIME.length() });
			} else if (SubStyleType.DATETIME.equals(type)) {
				p = new ParseDate(DateUtils.FORMAT_DATE_TIME);
			} else if (SubStyleType.TIMESTAMP.equals(type)) {
				p = new ParseDate(DateUtils.FORMAT_DATE_TIME);
			} else if (SubStyleType.DATE.equals(type)) {
				p = new ParseDate(DateUtils.FORMAT_DATE);
			} else if (SubStyleType.TIME.equals(type)) {
				p = new ParseDate(DateUtils.FORMAT_TIME);
			}

			if (p instanceof StringCellProcessor) {
				p = new Trim((StringCellProcessor) p);
			}

			if (c.isNullable()) {
				p = new Optional(p);
			} else {
				p = new NotNull(p);
			}
			plist.add(p);
		}

		return plist.toArray(new CellProcessor[0]);
	}

}
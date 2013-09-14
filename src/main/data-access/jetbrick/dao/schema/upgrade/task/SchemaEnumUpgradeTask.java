package jetbrick.dao.schema.upgrade.task;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import jetbrick.commons.lang.DateUtils;
import jetbrick.commons.xml.XmlNode;
import jetbrick.dao.schema.data.EntityUtils;
import jetbrick.dao.schema.upgrade.UpgradeLogger;
import jetbrick.dao.schema.upgrade.model.SchemaChecksum;
import jetbrick.dao.schema.upgrade.model.SchemaEnum;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 枚举变量的升降级
 */
public class SchemaEnumUpgradeTask extends UpgradeTask {
    private final Logger log = LoggerFactory.getLogger(SchemaEnumUpgradeTask.class);
    private final String SCHEMA_ENUM_FILE = "/META-INF/schema-enum.xml";

    private final List<SchemaChecksum> enumGroupQueue = new ArrayList<SchemaChecksum>();
    private final List<SchemaEnum> enumQueue = new ArrayList<SchemaEnum>();

    public SchemaEnumUpgradeTask(UpgradeLogger fileLog) {
        super(fileLog);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        ensureCreate();

        // 读取数据库中存在的 Enum checksum
        Map<String, SchemaChecksum> db_checksum_map = ListOrderedMap.decorate(new CaseInsensitiveMap());
        List<SchemaChecksum> db_checksum_list = SchemaChecksum.DAO.loadSomeEx("type", "ENUM");
        for (SchemaChecksum checksum : db_checksum_list) {
            db_checksum_map.put(checksum.getName(), checksum);
        }

        // 读取当前的ENUM文件，并比较是否有变化
        InputStream fileXml = getClass().getResourceAsStream(SCHEMA_ENUM_FILE);
        XmlNode root = XmlNode.create(fileXml);
        for (XmlNode node : root.elements()) {
            Integer g_pid = node.attribute("pid").asInt();
            String g_checksum = node.attribute("checksum").asString();

            String name = "enum_group_" + g_pid;
            SchemaChecksum checksum = db_checksum_map.get(name);
            if (checksum == null || !StringUtils.equals(checksum.getChecksum(), g_checksum)) {
                if (checksum == null) {
                    checksum = SchemaChecksum.newInstance();
                    checksum.setName(name);
                    checksum.setType("ENUM");
                }
                checksum.setChecksum(g_checksum);
                checksum.setTimestamp(new Date());
                enumGroupQueue.add(checksum);

                // update
                for (XmlNode child : node.elements()) {
                    SchemaEnum info = SchemaEnum.newInstance();
                    info.setId(child.attribute("id").asInt());
                    info.setPid(g_pid);
                    info.setName(child.attribute("name").asString());
                    info.setDefineName(child.attribute("var").asString());
                    info.setDescription(child.attribute("desc").asString());
                    enumQueue.add(info);
                }
            }
        }

        db_checksum_list.clear();
        db_checksum_map.clear();
    }

    @Override
    public void destory() {
        enumQueue.clear();
    }

    @Override
    public boolean isRequired() {
        if (enumGroupQueue.size() == 0 && enumQueue.size() == 0) {
            log.info("Database SchemaEnum upgrade is not required.");
        }
        return enumGroupQueue.size() > 0;
    }

    @Override
    public void execute() {
        fileLog.println(">>>> SchemaEnum Upgrade checking ...");
        fileLog.println(">>>> date = %s", DateUtils.getNowStr());
        fileLog.println("");

        List<SchemaEnum> new_queue = new ArrayList<SchemaEnum>();
        List<SchemaEnum> update_queue = new ArrayList<SchemaEnum>();

        Map<Serializable, SchemaEnum> db_enum_map = EntityUtils.map(SchemaEnum.DAO.loadAll());

        for (SchemaEnum xml_en : enumQueue) {
            SchemaEnum db_en = db_enum_map.get(xml_en.getId());
            if (db_en == null) {
                new_queue.add(xml_en);
            } else {
                EqualsBuilder builder = new EqualsBuilder();
                builder.append(xml_en.getName(), db_en.getName());
                builder.append(xml_en.getDefineName(), db_en.getDefineName());
                builder.append(xml_en.getDescription(), db_en.getDescription());
                if (!builder.isEquals()) {
                    update_queue.add(xml_en);
                }
            }
        }

        if (new_queue.size() > 0) {
            SchemaEnum.DAO.saveAll(new_queue);
        }
        if (update_queue.size() > 0) {
            SchemaEnum.DAO.updateAll(update_queue);
        }

        // update checksum for enum group
        SchemaChecksum.DAO.saveOrUpdateAll(enumGroupQueue);

        fileLog.println(">>>> SchemaEnum Upgrade completed.\n");
        fileLog.println(">>>> Total: %d inserted, %d updated.\n", new_queue.size(), update_queue.size());
        fileLog.println("");
    }

    private void ensureCreate() {
        if (!SchemaEnum.DAO.tableExist()) {
            SchemaEnum.DAO.tableCreate();
        }
    }
}

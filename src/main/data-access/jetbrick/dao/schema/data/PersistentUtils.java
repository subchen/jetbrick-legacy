package jetbrick.dao.schema.data;

import java.io.InputStream;
import java.util.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.commons.xml.XmlNode;
import org.apache.commons.collections.map.ListOrderedMap;

/**
 * 用来管理所有的 PersistentData
 */
public class PersistentUtils {
    private static final String SCHEMA_FILE = "/META-INF/schema-table.xml";
    private static final ListOrderedMap schemas = new ListOrderedMap();
    private static final ListOrderedMap caches = new ListOrderedMap();

    static {
        try {
            InputStream schemaXml = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_FILE);

            XmlNode root = XmlNode.create(schemaXml);
            for (XmlNode node : root.elements()) {
                try {
                    Class<?> schemaClass = node.attribute("class").asClass();
                    schemas.put(schemaClass, schemaClass.getDeclaredField("SCHEMA").get(null));
                    caches.put(schemaClass, schemaClass.getDeclaredField("CACHE").get(null));
                } catch (Exception e) {
                    throw SystemException.unchecked(e);
                }
            }
        } catch (Exception e) {
            throw SystemException.unchecked(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Class<? extends PersistentData>> getSchemaClassList() {
        return schemas.keyList();
    }

    @SuppressWarnings("unchecked")
    public static List<SchemaInfo<? extends PersistentData>> getSchemaList() {
        return schemas.valueList();
    }

    @SuppressWarnings("unchecked")
    public static <T extends PersistentData> SchemaInfo<T> getSchema(Class<T> schemaClass) {
        SchemaInfo<T> schema = (SchemaInfo<T>) schemas.get(schemaClass);
        if (schema == null) {
            // for SchemaChecksum, SchemaEnum ...
            try {
                schema = (SchemaInfo<T>) schemaClass.getDeclaredField("SCHEMA").get(null);
            } catch (Exception e) {
                throw SystemException.unchecked(e);
            }
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    public static <T extends PersistentData> PersistentCache<T> getCache(Class<T> schemaClass) {
        PersistentCache<T> cache = (PersistentCache<T>) caches.get(schemaClass);
        if (cache == null) {
            // for SchemaChecksum, SchemaEnum ...
            cache = PersistentCache.NO_CACHE;
        }
        return cache;
    }

    public static <T extends PersistentData> Map<Long, T> map(List<T> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return Collections.emptyMap();
        }
        Map<Long, T> map = new HashMap<Long, T>();
        for (T data : dataList) {
            map.put(data.getId(), data);
        }
        return map;
    }
}

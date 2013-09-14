package jetbrick.dao.schema.data;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.commons.xml.XmlNode;
import jetbrick.dao.id.*;
import jetbrick.dao.orm.DataSourceUtils;
import jetbrick.dao.orm.jdbc.RowMapper;
import jetbrick.dao.schema.upgrade.model.SchemaChecksum;
import jetbrick.dao.schema.upgrade.model.SchemaEnum;
import org.apache.commons.collections.map.ListOrderedMap;

public class EntityUtils {
    private static final String SCHEMA_FILE = "/META-INF/schema-table.xml";
    private static final ListOrderedMap schema_map = new ListOrderedMap();
    private static final Map<Class<?>, EntityCache<?>> cache_map = new HashMap();
    private static final Map<Class<?>, RowMapper<?>> row_mapper_map = new HashMap();
    private static final Map<Class<?>, EntityDaoHelper<?>> dao_helper_map = new HashMap();
    private static final SequenceIdProvider seq_id_provider = new JdbcSequenceIdProvider(DataSourceUtils.getDataSource());

    static {
        try {
            InputStream schemaXml = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_FILE);

            List<Class<?>> entityClassList = new ArrayList();
            entityClassList.add(SchemaChecksum.class);
            entityClassList.add(SchemaEnum.class);

            XmlNode root = XmlNode.create(schemaXml);
            for (XmlNode node : root.elements()) {
                try {
                    Class<?> entityClass = node.attribute("class").asClass();
                    entityClassList.add(entityClass);
                } catch (Throwable e) {
                    throw SystemException.unchecked(e);
                }
            }

            for (Class<?> entityClass : entityClassList) {
                schema_map.put(entityClass, entityClass.getDeclaredField("SCHEMA").get(null));
                cache_map.put(entityClass, (EntityCache<?>) entityClass.getDeclaredField("CACHE").get(null));
                dao_helper_map.put(entityClass, (EntityDaoHelper<?>) entityClass.getDeclaredField("DAO").get(null));

                Field field = entityClass.getDeclaredField("ROW_MAPPER");
                if (field != null) {
                    row_mapper_map.put(entityClass, (RowMapper<?>) field.get(null));
                }
            }

        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    public static List<Class<? extends Entity>> getEntityClassList() {
        return schema_map.keyList();
    }

    public static List<SchemaInfo<? extends Entity>> getSchemaList() {
        return schema_map.valueList();
    }

    public static <T extends Entity> SchemaInfo<T> getSchema(Class<T> entityClass) {
        return (SchemaInfo<T>) schema_map.get(entityClass);
    }

    public static <T extends Entity> EntityCache<T> getEntityCache(Class<T> entityClass) {
        return (EntityCache<T>) cache_map.get(entityClass);
    }

    public static <T extends Entity> EntityDaoHelper<T> getEntityDaoHelper(Class<T> entityClass) {
        return (EntityDaoHelper<T>) dao_helper_map.get(entityClass);
    }

    public static <T extends Entity> RowMapper<T> getEntityRowMapper(Class<T> entityClass) {
        return (RowMapper<T>) row_mapper_map.get(entityClass);
    }

    public static String getTableName(Class<? extends Entity> entityClass) {
        return getSchema(entityClass).getTableName();
    }

    public static String getColumnName(Class<? extends Entity> entityClass, String fieldName) {
        SchemaColumn c = getSchema(entityClass).getColumn(fieldName);
        return c == null ? null : c.getColumnName();
    }

    public static SequenceId createSequenceId(Class<? extends Entity> entityClass) {
        return seq_id_provider.create(getTableName(entityClass));
    }

    public static <T extends Entity> Map<Serializable, T> map(List<T> entities) {
        if (entities == null || entities.size() == 0) {
            return Collections.emptyMap();
        }
        Map<Serializable, T> map = new HashMap<Serializable, T>();
        for (T entity : entities) {
            map.put(entity.getId(), entity);
        }
        return map;
    }
}

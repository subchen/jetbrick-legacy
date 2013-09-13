package jetbrick.dao.schema.data;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.commons.xml.XmlNode;
import jetbrick.dao.id.*;
import jetbrick.dao.orm.RowMapper;
import jetbrick.dao.schema.data.orm.EntityDaoHelper;
import jetbrick.dao.utils.DataSourceUtils;
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

            XmlNode root = XmlNode.create(schemaXml);
            for (XmlNode node : root.elements()) {
                try {
                    Class<?> entityClass = node.attribute("class").asClass();
                    schema_map.put(entityClass, entityClass.getDeclaredField("SCHEMA").get(null));
                    cache_map.put(entityClass, (EntityCache<?>) entityClass.getDeclaredField("CACHE").get(null));
                    row_mapper_map.put(entityClass, (RowMapper<?>) entityClass.getDeclaredField("ROW_MAPPER").get(null));
                    dao_helper_map.put(entityClass, (EntityDaoHelper<?>) entityClass.getDeclaredField("DAO").get(null));
                } catch (Throwable e) {
                    throw SystemException.unchecked(e);
                }
            }
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Class<? extends Entity>> getEntityClassList() {
        return schema_map.keyList();
    }

    @SuppressWarnings("unchecked")
    public static List<SchemaInfo<? extends Entity>> getSchemaList() {
        return schema_map.valueList();
    }

    public static <T extends Entity> SchemaInfo<T> getSchema(Class<T> entityClass) {
        SchemaInfo<T> schema = (SchemaInfo<T>) schema_map.get(entityClass);
        if (schema == null) {
            // for SchemaChecksum, SchemaEnum ...
            try {
                Field field = entityClass.getDeclaredField("SCHEMA");
                field.setAccessible(true);
                schema = (SchemaInfo<T>) field.get(null);
                schema_map.put(entityClass, schema);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
        return schema;
    }

    public static <T extends Entity> EntityCache<T> getEntityCache(Class<T> entityClass) {
        EntityCache<T> cache = (EntityCache<T>) cache_map.get(entityClass);
        if (cache == null) {
            // for SchemaChecksum, SchemaEnum ...
            try {
                Field field = entityClass.getDeclaredField("CACHE");
                field.setAccessible(true);
                cache = (EntityCache<T>) field.get(null);
                cache_map.put(entityClass, cache);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
        return cache;
    }

    public static <T extends Entity> EntityDaoHelper getEntityDaoHelper(Class<T> entityClass) {
        EntityDaoHelper dao = dao_helper_map.get(entityClass);
        if (dao == null) {
            // for SchemaChecksum, SchemaEnum ...
            try {
                Field field = entityClass.getDeclaredField("DAO");
                field.setAccessible(true);
                dao = (EntityDaoHelper) field.get(null);
                dao_helper_map.put(entityClass, dao);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
        return dao;
    }

    public static <T extends Entity> RowMapper<T> getEntityRowMapper(Class<T> entityClass) {
        RowMapper<T> mapper = (RowMapper<T>) row_mapper_map.get(entityClass);
        if (mapper == null) {
            // for SchemaChecksum, SchemaEnum ...
            try {
                Field field = entityClass.getDeclaredField("ROW_MAPPER");
                field.setAccessible(true);
                mapper = (RowMapper<T>) field.get(null);
                row_mapper_map.put(entityClass, mapper);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
        return mapper;
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

    public static <T extends Entity> Map<Integer, T> map(List<T> entities) {
        if (entities == null || entities.size() == 0) {
            return Collections.emptyMap();
        }
        Map<Integer, T> map = new HashMap<Integer, T>();
        for (T entity : entities) {
            map.put(entity.getId(), entity);
        }
        return map;
    }
}

package jetbrick.dao.schema.data;

import java.util.*;
import jetbrick.commons.exception.SystemException;
import jetbrick.dao.oam.JdbcTemplate;
import jetbrick.dao.oam.RowMapper;
import jetbrick.dao.oam.handler.PagelistHandler;
import jetbrick.dao.oam.utils.SqlUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 负责通用的 PersistentData 对象的 DAO 操作 （带缓存支持）
 * 单例模式
 */
public class PersistentDAO extends PersistentJdbcTemplate {
    public PersistentDAO(JdbcTemplate jdbc) {
        super(jdbc);
    }

    public <T extends PersistentData> int save(T data) {
        data.generateId();
        data.validate();
        int result = jdbc_save(data);

        @SuppressWarnings("unchecked")
        PersistentCache<T> cache = (PersistentCache<T>) data.getCache();
        cache.put(data);
        cache.evictAllSqls();

        return result;
    }

    public <T extends PersistentData> int update(T data) {
        data.validate();
        int result = jdbc_update(data);

        @SuppressWarnings("unchecked")
        PersistentCache<T> cache = (PersistentCache<T>) data.getCache();
        cache.put(data);

        return result;
    }

    public int saveOrUpdate(PersistentData data) {
        if (data.getId() == null) {
            return save(data);
        } else {
            return update(data);
        }
    }

    public int delete(Class<? extends PersistentData> schemaClass, Long id) {
        int result = jdbc_delete(schemaClass, id);

        PersistentCache<? extends PersistentData> cache = PersistentUtils.getCache(schemaClass);
        cache.evict(id);
        cache.evictAllSqls();

        return result;
    }

    public int delete(PersistentData data) {
        int result = jdbc_delete(data);

        PersistentCache<? extends PersistentData> cache = data.getCache();
        cache.evict(data.getId());
        cache.evictAllSqls();

        return result;
    }

    //-------------------------------------------------------
    public int[] saveAll(PersistentData... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = save(datalist[i]);
        }
        return results;
    }

    public int[] updateAll(PersistentData... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = update(datalist[i]);
        }
        return results;
    }

    public int[] saveOrUpdateAll(PersistentData... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = saveOrUpdate(datalist[i]);
        }
        return results;
    }

    public int[] deleteAll(PersistentData... datalist) {
        if (datalist.length == 0) return ArrayUtils.EMPTY_INT_ARRAY;

        int[] results = new int[datalist.length];
        for (int i = 0; i < datalist.length; i++) {
            results[i] = delete(datalist[i]);
        }
        return results;
    }

    //-------------------------------------------------------

    /**
     * 批量保存， 必须保证对象是相同的Class
     */
    public <T extends PersistentData> int[] batchSave(List<T> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        PersistentData first = dataList.get(0);
        String sql = sqls_cache_insert.get(first.getClass());
        if (sql == null) {
            sql = get_sql_insert(first.getSchema());
            sqls_cache_insert.put(first.getClass(), sql);
        }

        List<Object[]> parameters = new ArrayList(dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            PersistentData data = dataList.get(i);
            data.generateId();
            data.validate();

            if (data.getSchema() != first.getSchema()) {
                throw new SystemException("dataList 存在不同的 Class， 无法进行 batchSave");
            }
            parameters.add(data.dao_insert_parameters());
        }

        int[] results = jdbc.batchUpdate(sql, parameters);

        @SuppressWarnings("unchecked")
        PersistentCache<T> cache = (PersistentCache<T>) first.getCache();
        cache.putAll(dataList);

        return results;
    }

    /**
     * 批量更新， 必须保证对象是相同的Class
     */
    public <T extends PersistentData> int[] batchUpdate(List<T> dataList) {
        if (dataList == null || dataList.size() == 0) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }

        PersistentData first = dataList.get(0);
        String sql = sqls_cache_update.get(first.getClass());
        if (sql == null) {
            sql = get_sql_update(first.getSchema());
            sqls_cache_update.put(first.getClass(), sql);
        }

        List<Object[]> parameters = new ArrayList(dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            PersistentData data = dataList.get(i);
            data.validate();

            if (data.getSchema() != first.getSchema()) {
                throw new SystemException("dataList 存在不同的 Class， 无法进行 batchSave");
            }
            parameters.add(data.dao_update_parameters());
        }

        int[] results = jdbc.batchUpdate(sql, parameters);

        @SuppressWarnings("unchecked")
        PersistentCache<T> cache = (PersistentCache<T>) first.getCache();
        cache.putAll(dataList);

        return results;
    }

    //---------------------------------------------------------------------------
    public <T extends PersistentData> T get(Class<T> schemaClass, Long id) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);
        T data = cache.get(id);
        if (data == null) {
            data = cache.put(jdbc_get(schemaClass, id));
        }
        return data;
    }

    public <T extends PersistentData> T get(Class<T> schemaClass, String name, Object value) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);

        String key = cache.key("get", "", name, value);
        Long[] ids = cache.get(key);
        if (ids == null) {
            SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
            String tableName = dialect.getIdentifier(schema.getTableName());
            String columnName = dialect.getIdentifier(name);
            String sql = "select * from " + tableName + " where " + columnName + "=?";

            T result = jdbc.queryAsObject(schemaClass, sql, value);
            return cache.put(key, result);
        } else {
            return get(schemaClass, ids[0]);
        }
    }

    public <T extends PersistentData> List<T> getSome(Class<T> schemaClass, String name, Object value, String... sort) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);

        String key = cache.key("getSome", "", name, value, sort);
        Long[] ids = cache.get(key);
        if (ids == null) {
            SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
            String tableName = dialect.getIdentifier(schema.getTableName());
            String columnName = dialect.getIdentifier(name);
            String sql = "select * from " + tableName + " where " + columnName + "=?";

            List<T> result = jdbc.queryAsList(schemaClass, sql, value);
            return cache.put(key, result);
        } else {
            return getSome(schemaClass, ids[0]);
        }
    }

    public <T extends PersistentData> List<T> getSome(Class<T> schemaClass, Long... ids) {
        if (ids == null || ids.length == 0) return Collections.emptyList();

        final List<Long> ids_pos = new ArrayList<Long>(ids.length);
        final List<T> results = new ArrayList<T>(ids.length);

        for (Long id : ids) {
            ids_pos.add(id);
            results.add(null);
        }

        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);
        final List<Long> no_cache_ids = new ArrayList<Long>();
        for (int i = 0; i < ids.length; i++) {
            T data = (T) cache.get(ids[i]);
            if (data != null) {
                results.set(i, data);
            } else {
                no_cache_ids.add(ids[i]);
            }
        }

        if (no_cache_ids.size() > 0) {
            List<T> no_cache_objs = jdbc_get_some(schemaClass, (Long[]) no_cache_ids.toArray());
            for (T data : no_cache_objs) {
                results.set(ids_pos.indexOf(data.getId()), data);
            }
        }

        return cache.putAll(results);
    }

    public <T extends PersistentData> List<T> getAll(Class<T> schemaClass, String... sort) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);

        String key = cache.key("getAll", "", (Object[]) sort);
        Long[] ids = cache.get(key);
        if (ids == null) {
            SchemaInfo<T> schema = PersistentUtils.getSchema(schemaClass);
            String tableName = dialect.getIdentifier(schema.getTableName());
            String sql = "select * from " + tableName;
            if (sort != null && sort.length > 0) {
                sql = sql + " order by " + StringUtils.join(sort, ",");
            }
            List<T> result = jdbc.queryAsList(schemaClass, sql);
            return cache.put(key, result);
        } else {
            return getSome(schemaClass, ids);
        }
    }

    //-------------------------------------------------------
    public <T extends PersistentData> T queryAsObject(Class<T> schemaClass, String sql, Object... parameters) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);

        String key = cache.key("queryAsObject", sql, parameters);
        Long[] ids = cache.get(key);
        if (ids == null) {
            T result = jdbc.queryAsObject(schemaClass, sql, parameters);
            return cache.put(key, result);
        } else {
            return get(schemaClass, ids[0]);
        }
    }

    public <T extends PersistentData> List<T> queryAsList(Class<T> schemaClass, String sql, Object... parameters) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);

        String key = cache.key("queryAsList", sql, parameters);
        Long[] ids = cache.get(key);
        if (ids == null) {
            List<T> dataList = jdbc.queryAsList(schemaClass, sql, parameters);
            return cache.put(key, dataList);
        } else {
            return cache.put(key, getSome(schemaClass, ids));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentData> Pagelist queryAsPageList(Pagelist pagelist, Class<T> schemaClass, String sql, Object... parameters) {
        PersistentCache<T> cache = PersistentUtils.getCache(schemaClass);

        if (pagelist.getCount() < 0) {
            String key = cache.key("queryAsPageList-count", sql, parameters);
            Integer count = (Integer) cache.getObject(key);
            if (count == null) {
                String count_sql = SqlUtils.sql_get_count(sql);
                count = queryAsInt(count_sql, parameters);
                cache.putObject(key, count);
            }
            pagelist.setCount(count);
        }

        List<?> items;
        String key = cache.key("queryAsPageList-items", sql, parameters);
        Long[] ids = cache.get(key);
        if (ids == null) {
            items = Collections.emptyList();
            if (pagelist.getCount() > 0) {
                RowMapper<T> rowMapper = jdbc.getRowMapper(schemaClass);
                PagelistHandler<T> rsh = new PagelistHandler<T>(rowMapper);
                rsh.setFirstResult(pagelist.getFirstResult());
                rsh.setMaxResults(pagelist.getPageSize());
                items = jdbc.query(rsh, sql, parameters);
            }
        } else {
            items = getSome(schemaClass, ids);
        }

        pagelist.setItems(items);
        cache.putAll((List<T>) items);

        return pagelist;
    }

}

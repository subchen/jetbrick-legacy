package jetbrick.dao.schema.data;

import java.util.*;
import jetbrick.dao.orm.JdbcTemplate;
import jetbrick.dao.orm.handler.PagelistHandler;
import jetbrick.dao.orm.mapper.SingleColumnRowMapper;
import jetbrick.dao.orm.utils.SqlUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.ArrayUtils;

public class CachedEntityDaoHelper extends EntityDaoHelper {

    public CachedEntityDaoHelper(JdbcTemplate jdbc) {
        super(jdbc);
    }

    // -------- dao read ------------------------------------
    @Override
    public <T extends Entity> T load(Class<T> entityClass, Integer id) {
        EntityCache cache = EntityUtils.getEntityCache(entityClass);
        T entity = (T) cache.getEntity(id);
        if (entity == null) {
            entity = super.load(entityClass, id);
            if (entity != null) {
                cache.addEntity(entity);
            }
        }
        return entity;
    }

    @Override
    public <T extends Entity> List<T> loadSome(Class<T> entityClass, Integer... ids) {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }

        EntityCache cache = EntityUtils.getEntityCache(entityClass);
        final List<Integer> no_cache_ids = new ArrayList<Integer>();
        final ListOrderedMap result_map = new ListOrderedMap();
        for (int i = 0; i < ids.length; i++) {
            Integer id = ids[i];
            Entity entity = (Entity) cache.getEntity(id);
            result_map.put(id, entity);

            if (entity != null) {
                no_cache_ids.add(id);
            }
        }

        if (no_cache_ids.size() > 0) {
            List<T> no_cache_entities = super.loadSome(entityClass, (Integer[]) no_cache_ids.toArray());
            for (T entity : no_cache_entities) {
                result_map.put(entity.getId(), entity);
                cache.addEntity(entity);
            }
        }

        return result_map.valueList();
    }

    @Override
    public <T extends Entity> T queryAsObject(Class<T> entityClass, String sql, Object... parameters) {
        EntityCache cache = EntityUtils.getEntityCache(entityClass);

        Object key = cache.createCacheKey("object", sql, parameters);
        Integer[] ids = cache.getEntityIds(key);
        T entity = null;
        if (ids == null) {
            entity = super.queryAsObject(entityClass, sql, parameters);
            if (entity != null) {
                cache.addEntity(entity);
                cache.addEntityIds(key, new Integer[] { entity.getId() });
            } else {
                cache.addEntityIds(key, ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY);
            }
        } else {
            if (ids.length > 0) {
                entity = load(entityClass, ids[0]);
            }
        }
        return entity;
    }

    @Override
    public <T extends Entity> List<T> queryAsList(Class<T> entityClass, String sql, Object... parameters) {
        EntityCache cache = EntityUtils.getEntityCache(entityClass);
        Object key = cache.createCacheKey("list", sql, parameters);
        Integer[] ids = cache.getEntityIds(key);
        if (ids == null) {
            sql = sql_select_ids(sql);
            ids = super.queryAsArray(Integer.class, sql, parameters);
            cache.addEntityIds(key, ids);
        }
        return loadSome(entityClass, ids);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> Pagelist queryAsPageList(Pagelist pagelist, Class<T> entityClass, String sql, Object... parameters) {
        EntityCache cache = EntityUtils.getEntityCache(entityClass);
        if (pagelist.getCount() < 0) {
            Object key = cache.createCacheKey("pagelist-count", sql, parameters);
            Integer[] counts = cache.getEntityIds(key);
            if (counts == null) {
                String count_sql = SqlUtils.sql_get_count(sql);
                Integer count = queryAsInt(count_sql, parameters);
                counts = new Integer[] { count };
                cache.addEntityIds(key, counts);
            }
            pagelist.setCount(counts[0]);
        }

        if (pagelist.getCount() > 0) {
            Object key = cache.createCacheKey("pagelist-items", sql, parameters);
            Integer[] ids = (Integer[]) cache.getEntityIds(key);
            if (ids == null) {
                PagelistHandler<Integer> rsh = new PagelistHandler<Integer>(new SingleColumnRowMapper(Integer.class));
                rsh.setFirstResult(pagelist.getFirstResult());
                rsh.setMaxResults(pagelist.getPageSize());
                sql = sql_select_ids(sql);
                List<Integer> items = jdbc.query(rsh, sql, parameters);
                ids = (Integer[]) items.toArray();
                cache.addEntityIds(key, ids);
            }
            List<T> items = loadSome(entityClass, ids);
            pagelist.setItems(items);
        }

        return pagelist;
    }

    // -------- dao write ------------------------------------
    @Override
    public int save(Entity entity) {
        EntityCache cache = EntityUtils.getEntityCache(entity.getClass());
        cache.deleteEntityIds();

        int n = super.save(entity);
        cache.addEntity(entity);
        return n;
    }

    @Override
    public int update(Entity entity) {
        int n = super.update(entity);

        EntityCache cache = EntityUtils.getEntityCache(entity.getClass());
        cache.addEntity(entity);
        return n;
    }

    @Override
    public int delete(Entity entity) {
        EntityCache cache = EntityUtils.getEntityCache(entity.getClass());
        cache.deleteEntityIds();
        cache.deleteEntity(entity.getId());
        return super.delete(entity);
    }

    // ------------------------------------------------
    protected String sql_select_ids(String sql) {
        int pos = sql.toLowerCase().replaceAll("[\\n|\\r|\\t]", " ").indexOf(" from ");
        return "select id from " + sql.substring(pos);
    }
}

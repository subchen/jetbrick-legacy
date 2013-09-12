package jetbrick.dao.schema.data;

import java.util.*;
import jetbrick.dao.orm.JdbcHelper;
import jetbrick.dao.orm.handler.PagelistHandler;
import jetbrick.dao.orm.mapper.SingleColumnRowMapper;
import jetbrick.dao.orm.utils.SqlUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.ArrayUtils;

public class CachedEntityDaoHelper<T extends Entity> extends EntityDaoHelper<T> {
    protected final EntityCache<T> cache;

    public CachedEntityDaoHelper(JdbcHelper jdbc, Class<T> entityClass) {
        super(jdbc, entityClass);
        this.cache = EntityUtils.getEntityCache(entityClass);
    }

    // -------- save/update/delete ------------------------------------
    @Override
    public int save(T entity) {
        cache.deleteEntityIds();
        int result = super.save(entity);
        cache.addEntity(entity);
        return result;
    }

    @Override
    public int update(T entity) {
        int result = super.update(entity);
        cache.addEntity(entity);
        return result;
    }

    @Override
    public int delete(Integer id) {
        cache.deleteEntityIds();
        cache.deleteEntity(id);
        return jdbc.execute(sql_delete, id);
    }

    // -------- batch save/update/delete ---------------------------------
    @Override
    public void saveAll(T... entities) {
        cache.deleteEntityIds();
        super.saveAll(entities);
        cache.addEntities(entities);
    }

    @Override
    public void updateAll(T... entities) {
        super.updateAll(entities);
        cache.addEntities(entities);
    }

    @Override
    public void saveOrUpdateAll(T... entities) {
        if (entities.length == 0) return;

        cache.deleteEntityIds();
        for (T entity : entities) {
            if (entity.getId() == null) {
                save(entity);
            } else {
                update(entity);
            }
            cache.addEntity(entity);
        }
    }

    @Override
    public int deleteAll(Integer... ids) {
        cache.deleteEntityIds();
        for (Integer id : ids) {
            cache.deleteEntity(id);
        }
        return super.deleteAll(ids);
    }

    // -------- load/query ------------------------------------
    @Override
    public T load(Integer id) {
        T entity = (T) cache.getEntity(id);
        if (entity == null) {
            entity = super.load(id);
            if (entity != null) {
                cache.addEntity(entity);
            }
        }
        return entity;
    }

    @Override
    public List<T> loadSome(Integer... ids) {
        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }

        final List<Integer> no_cache_ids = new ArrayList<Integer>();
        final ListOrderedMap result_map = new ListOrderedMap();
        for (int i = 0; i < ids.length; i++) {
            Integer id = ids[i];
            T entity = cache.getEntity(id);
            result_map.put(id, entity);

            if (entity != null) {
                no_cache_ids.add(id);
            }
        }

        if (no_cache_ids.size() > 0) {
            List<T> no_cache_entities = super.loadSome((Integer[]) no_cache_ids.toArray());
            for (T entity : no_cache_entities) {
                result_map.put(entity.getId(), entity);
                cache.addEntity(entity);
            }
        }

        return result_map.valueList();
    }

    @Override
    public T queryAsObject(String sql, Object... parameters) {
        Object key = cache.createCacheKey("object", sql, parameters);
        Integer[] ids = cache.getEntityIds(key);
        T entity = null;
        if (ids == null) {
            entity = jdbc.queryAsObject(rowMapper, sql, parameters);
            if (entity != null) {
                cache.addEntity(entity);
                cache.addEntityIds(key, new Integer[] { entity.getId() });
            } else {
                cache.addEntityIds(key, ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY);
            }
        } else {
            if (ids.length > 0) {
                entity = load(ids[0]);
            }
        }
        return entity;
    }

    @Override
    public List<T> queryAsList(String sql, Object... parameters) {
        Object key = cache.createCacheKey("list", sql, parameters);
        Integer[] ids = cache.getEntityIds(key);
        if (ids == null) {
            sql = get_sql_select_ids(sql);
            ids = jdbc.queryAsArray(Integer.class, sql, parameters);
            cache.addEntityIds(key, ids);
        }
        return loadSome(ids);
    }

    @Override
    public Pagelist queryAsPageList(Pagelist pagelist, String sql, Object... parameters) {
        if (pagelist.getCount() < 0) {
            Object key = cache.createCacheKey("pagelist-count", sql, parameters);
            Integer[] counts = cache.getEntityIds(key);
            if (counts == null) {
                String count_sql = SqlUtils.sql_get_count(sql);
                Integer count = jdbc.queryAsInt(count_sql, parameters);
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
                sql = get_sql_select_ids(sql);
                List<Integer> items = jdbc.query(rsh, sql, parameters);
                ids = (Integer[]) items.toArray();
                cache.addEntityIds(key, ids);
            }
            List<T> items = loadSome(ids);
            pagelist.setItems(items);
        }

        return pagelist;
    }

    // ----- sql gen ---------------------------------------------
    private String get_sql_select_ids(String sql) {
        sql = sql.replaceAll("\\s+", " ");
        int pos = sql.toLowerCase().indexOf(" from ");
        return "select id from " + sql.substring(pos);
    }
}

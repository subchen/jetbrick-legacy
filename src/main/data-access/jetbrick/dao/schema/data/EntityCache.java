package jetbrick.dao.schema.data;

import java.util.List;
import jetbrick.commons.cache.*;
import jetbrick.commons.cache.ehcache.EhCacheProvider;

public class EntityCache<T extends Entity> {
    public static final EntityCache NO_CACHE = new EntityCache();

    private final Cache id_cache;
    private final Cache page_cache;

    public EntityCache(String name, int maxElementsInMemory, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds) {
        CacheProvider provider = EhCacheProvider.getInstance();
        id_cache = provider.create(name + ".IDS", maxElementsInMemory, false, eternal, timeToLiveSeconds, timeToIdleSeconds);
        page_cache = provider.create(name + ".PAGES", 200, false, false, 1800, 1800);
    }

    private EntityCache() {
        id_cache = NoCache.NO_CACHE;
        page_cache = NoCache.NO_CACHE;
    }

    public T getEntity(Integer id) {
        return (T) id_cache.get(id);
    }

    public void addEntity(T entity) {
        id_cache.put(entity.getId(), entity);
    }

    public void addEntities(T... entities) {
        for (T entity : entities) {
            id_cache.put(entity.getId(), entity);
        }
    }

    public void addEntities(List<T> entities) {
        for (T entity : entities) {
            id_cache.put(entity.getId(), entity);
        }
    }

    public void deleteEntity(Integer id) {
        id_cache.remove(id);
    }

    public void deleteEntity(T entity) {
        id_cache.remove(entity.getId());
    }

    public void deleteEntities() {
        id_cache.clear();
    }

    public Integer[] getEntityIds(Object key) {
        return (Integer[]) page_cache.get(key);
    }

    public void addEntityIds(Object key, Integer[] ids) {
        page_cache.put(key, ids);
    }

    public void deleteEntityIds() {
        page_cache.clear();
    }

    public void clear() {
        id_cache.clear();
        page_cache.clear();
    }

    // -------- utils ------------------------------------
    public Object createCacheKey(String prefix, String sql, Object... parameters) {
        StringBuilder key = new StringBuilder();
        key.append(prefix).append("|");
        key.append(sql).append("|");
        for (Object parameter : parameters) {
            key.append(parameter).append("|");
        }
        return key.toString();
    }
}

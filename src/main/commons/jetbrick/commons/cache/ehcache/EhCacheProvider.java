package jetbrick.commons.cache.ehcache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jetbrick.commons.cache.Cache;
import jetbrick.commons.cache.CacheProvider;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

public class EhCacheProvider implements CacheProvider {
    private static EhCacheProvider instance = new EhCacheProvider();

    private static CacheManager manager = CacheManager.create();
    private Map<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

    public static EhCacheProvider getInstance() {
        return instance;
    }

    public Cache create(CacheConfiguration cacheConfiguration) {
        net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cacheConfiguration);
        manager.addCache(cache);
        return get(cache.getName());
    }

    @Override
    public Cache create(String name, int maxElementsInMemory, boolean overflowToDisk, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds) {
        net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(name, maxElementsInMemory, overflowToDisk, eternal, timeToLiveSeconds, timeToIdleSeconds);
        manager.addCache(cache);
        return get(cache.getName());
    }

    @Override
    public Cache get(String name) {
        Cache c = caches.get(name);
        if (c == null) {
            net.sf.ehcache.Cache cache = manager.getCache(name);
            if (cache == null) {
                throw new RuntimeException("Cache not found: " + name);
            }
            c = new EhCache(cache);
            caches.put(name, c);
        }
        return c;
    }

    @Override
    public void delete(String name) {
        manager.removeCache(name);
    }

    @Override
    public boolean exists(String name) {
        return manager.cacheExists(name);
    }
}

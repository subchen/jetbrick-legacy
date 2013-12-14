package jetbrick.commons.cache;

import java.util.Collections;
import java.util.List;

public class NoCache implements Cache {
    public static final Cache NO_CACHE = new NoCache();

    @Override
    public boolean exists(Object key) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public <K, V> V get(K key, CacheValueCreator<K, V> valueCreator) {
        return null;
    }

    @Override
    public <K, V> V get(K key, CacheValueCreator<K, V> valueCreator, int timeToLiveSeconds, int timeToIdleSeconds) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
    }

    @Override
    public void put(Object key, Object value, int timeToLiveSeconds, int timeToIdleSeconds) {
    }

    @Override
    public void remove(Object key) {
    }

    @Override
    public void clear() {
    }

    @Override
    public List<Object> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public int getSize() {
        return 0;
    }
}

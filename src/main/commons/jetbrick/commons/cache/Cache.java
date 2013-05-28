package jetbrick.commons.cache;

import java.util.List;

public interface Cache {

    public boolean exists(Object key);

    public Object get(Object key);

    public <K, V> V get(K key, CacheValueCreator<K, V> valueCreator);

    public <K, V> V get(K key, CacheValueCreator<K, V> valueCreator, int timeToLiveSeconds, int timeToIdleSeconds);

    public void put(Object key, Object value);

    public void put(Object key, Object value, int timeToLiveSeconds, int timeToIdleSeconds);

    public void remove(Object key);

    public void removeAll();

    public List<Object> getKeys();

    public int getSize();

}

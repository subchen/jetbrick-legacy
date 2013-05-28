package jetbrick.commons.cache;

public interface CacheValueCreator<K, V> {

    public V getValue(K key);

}

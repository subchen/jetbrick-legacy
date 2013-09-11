package jetbrick.commons.cache.ehcache;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import jetbrick.commons.cache.Cache;
import jetbrick.commons.cache.CacheValueCreator;
import net.sf.ehcache.Element;

public class EhCache implements Cache {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final net.sf.ehcache.Cache cache;

    public EhCache(net.sf.ehcache.Cache cache) {
        this.cache = cache;
    }

    @Override
    public boolean exists(Object key) {
        return cache.get(key) != null;
    }

    @Override
    public Object get(Object key) {
        lock.readLock().lock();
        try {
            Element element = cache.get(key);
            if (element == null) {
                return null;
            }
            return element.getObjectValue();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V get(K key, CacheValueCreator<K, V> valueCreator) {
        lock.readLock().lock();
        try {
            Element element = cache.get(key);
            if (element != null) return (V) element.getObjectValue();
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            Element element = cache.get(key);
            if (element != null) return (V) element.getObjectValue();

            V value = valueCreator.getValue(key);
            put(key, value);
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param key
     * @param valueCreator
     *            如果Key不存在，则自动从valueCreator中读取Value
     * @param timeToLiveSeconds
     *            以创建时间为基准开始计算的超时时长, 默认值是0，也就是时间无穷大
     * @param timeToIdleSeconds
     *            以最近访问时间作为基准计算的超时时长, 默认值是0，也就是时间无穷大
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K, V> V get(K key, CacheValueCreator<K, V> valueCreator, int timeToLiveSeconds, int timeToIdleSeconds) {
        lock.readLock().lock();
        try {
            Element element = cache.get(key);
            if (element != null) return (V) element.getObjectValue();
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            Element element = cache.get(key);
            if (element != null) return (V) element.getObjectValue();

            V value = valueCreator.getValue(key);
            put(key, value, timeToLiveSeconds, timeToIdleSeconds);
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void put(Object key, Object value) {
        lock.writeLock().lock();
        try {
            Element element = new Element(key, value);
            element.setEternal(true);
            cache.put(element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param key
     * @param value
     * @param timeToLiveSeconds
     *            以创建时间为基准开始计算的超时时长, 默认值是0，也就是时间无穷大
     * @param timeToIdleSeconds
     *            以最近访问时间作为基准计算的超时时长, 默认值是0，也就是时间无穷大
     * @return
     */
    @Override
    public void put(Object key, Object value, int timeToLiveSeconds, int timeToIdleSeconds) {
        lock.writeLock().lock();
        try {
            Element element = new Element(key, value);
            element.setEternal(false);
            element.setTimeToLive(timeToLiveSeconds);
            element.setTimeToIdle(timeToIdleSeconds);
            cache.put(element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(Object key) {
        lock.writeLock().lock();
        try {
            cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.removeAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getKeys() {
        lock.readLock().lock();
        try {
            return cache.getKeysWithExpiryCheck();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int getSize() {
        lock.readLock().lock();
        try {
            return cache.getSize();
        } finally {
            lock.readLock().unlock();
        }
    }

}

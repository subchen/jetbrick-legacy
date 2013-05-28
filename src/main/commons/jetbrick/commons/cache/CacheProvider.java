package jetbrick.commons.cache;

public interface CacheProvider {

    public Cache create(String name, int maxElementsInMemory, boolean overflowToDisk, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds);

    public Cache get(String name);

    public void delete(String name);

    public boolean exists(String name);

}

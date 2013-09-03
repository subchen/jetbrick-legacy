package jetbrick.dao.schema.data;

import java.util.List;
import jetbrick.commons.cache.Cache;
import jetbrick.commons.cache.ehcache.EhCacheProvider;
import org.apache.commons.lang.ArrayUtils;

/**
 * 负责对每个PersistentData类的查询的缓存
 */
public class PersistentCache<T extends PersistentData> {
	@SuppressWarnings("rawtypes")
	public static final PersistentCache NO_CACHE = new PersistentCache();

	private final Cache schema_cache;
	private final Cache sql_ids_cache;

	private PersistentCache() {
		schema_cache = null;
		sql_ids_cache = null;
	}

	public PersistentCache(String name, int maxElementsInMemory, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds) {
		EhCacheProvider provider = EhCacheProvider.getInstance();
		schema_cache = provider.create(name + ":SCHEMA", maxElementsInMemory, false, eternal, timeToLiveSeconds, timeToIdleSeconds);
		sql_ids_cache = provider.create(name + ":SQL_IDS", 200, false, false, 1800, 1800);
	}

	@SuppressWarnings("unchecked")
	public T get(Integer id) {
		if (schema_cache == null) return null;
		return (T) schema_cache.get(id);
	}

	//public <K extends PersistentData> K put(K data) {
	public T put(T data) {
		if (schema_cache != null) {
			schema_cache.put(data.getId(), data);
		}
		return data;
	}

	public List<T> putAll(List<T> results) {
		if (schema_cache != null) {
			for (T data : results) {
				schema_cache.put(data.getId(), data);
			}
		}
		return results;
	}

	public void evictAllSchema() {
		if (schema_cache != null) {
			schema_cache.removeAll();
		}
	}

	public void evict(Integer id) {
		if (schema_cache != null) {
			schema_cache.remove(id);
		}
	}

	public void evict(Integer... ids) {
		if (schema_cache != null) {
			for (Integer id : ids) {
				schema_cache.remove(id);
			}
		}
	}

	//-----------------------------------------------------------------
	public String key(String prefix, String sql, Object... parameters) {
		if (sql_ids_cache == null) return null;

		StringBuilder key = new StringBuilder();
		key.append(sql);
		for (Object parameter : parameters) {
			key.append(parameter);
		}
		return "sql:" + prefix + ":" + key.hashCode();
	}

	public Integer[] get(String key) {
		if (sql_ids_cache == null) return null;
		return (Integer[]) sql_ids_cache.get(key);
	}

	public List<T> put(String key, List<T> dataList) {
		if (sql_ids_cache != null) {
		    Integer[] ids = ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;

			if (dataList != null && dataList.size() > 0) {
				ids = new Integer[dataList.size()];
				for (int i = 0; i < ids.length; i++) {
					T data = dataList.get(i);
					ids[i] = data.getId();
					schema_cache.put(data.getId(), data);
				}
			}
			sql_ids_cache.put(key, ids);
		}
		return dataList;
	}

	public T put(String key, T data) {
		if (sql_ids_cache != null) {
			sql_ids_cache.put(key, new Integer[] { data.getId() });
		}
		if (schema_cache != null) {
			schema_cache.put(data.getId(), data);
		}
		return data;
	}

	public Object getObject(String key) {
		if (sql_ids_cache == null) return null;
		return sql_ids_cache.get(key);
	}

	public <K> K putObject(String key, K object) {
		if (sql_ids_cache != null) {
			sql_ids_cache.put(key, object);
		}
		return object;
	}

	public void evictAllSqls() {
		if (sql_ids_cache != null) {
			sql_ids_cache.removeAll();
		}
	}

}

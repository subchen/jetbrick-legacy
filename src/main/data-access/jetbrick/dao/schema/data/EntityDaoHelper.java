package jetbrick.dao.schema.data;

import java.util.Collection;
import java.util.List;
import jetbrick.dao.orm.Pagelist;

/**
 * 单表操作
 */
public interface EntityDaoHelper<T extends Entity> {

    // -------- table ---------------------------------
    public abstract boolean tableExist();

    public abstract int tableCreate();

    public abstract int tableDelete();

    // -------- save/update/delete ---------------------------------
    public abstract int save(T entity);

    public abstract int update(T entity);

    public abstract int saveOrUpdate(T entity);

    public abstract int delete(T entity);

    public abstract int delete(Integer id);

    // -------- batch save/update/delete ---------------------------------
    public abstract void saveAll(Collection<T> entities);

    public abstract void updateAll(Collection<T> entities);

    public abstract void saveOrUpdateAll(Collection<T> entities);

    public abstract void deleteAll(Collection<T> entities);

    public abstract int deleteAll(Integer... ids);

    // -------- load ---------------------------------
    public abstract T load(Integer id);

    public abstract T load(String name, Object value);

    public abstract List<T> loadSome(Integer... ids);

    public abstract List<T> loadSome(String name, Object value, String... sorts);

    public abstract List<T> loadAll(String... sorts);

    // -------- query ---------------------------------
    public abstract T queryAsObject(String sql, Object... parameters);

    public abstract List<T> queryAsList(String sql, Object... parameters);

    public abstract Pagelist queryAsPagelist(Pagelist pagelist, String sql, Object... parameters);

}

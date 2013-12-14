package jetbrick.commons.json;

import java.util.HashMap;
import java.util.Map;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.beanutils.PropertyUtils;

public class PropertyFilter implements JSONFilter {

    private Class<?> clazz;
    private String[] names;

    public PropertyFilter(Class<?> clazz, String... names) {
        this.clazz = clazz;
        this.names = names;
    }

    @Override
    public boolean accept(Class<?> clazz) {
        return this.clazz.isAssignableFrom(clazz);
    }

    @Override
    public String apply(Object object, JSONFilter[] filters) {
        try {
            Map<String, Object> map = new HashMap<String, Object>(names.length * 2);
            for (String name : names) {
                map.put(name, PropertyUtils.getSimpleProperty(object, name));
            }
            return JSON.toJSONString(map, filters);
        } catch (Exception e) {
            throw SystemException.unchecked(e);
        }
    }
}

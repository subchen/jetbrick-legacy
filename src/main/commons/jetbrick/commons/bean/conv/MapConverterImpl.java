package jetbrick.commons.bean.conv;

import java.util.Map;
import jetbrick.commons.bean.ClassConvertUtils;

/**
 * MapConverter 的默认实现
 */
public class MapConverterImpl extends MapConverter {
    protected Map<String, ?> map;

    public MapConverterImpl(Map<String, ?> map) {
        this.map = map;
    }

    @Override
    protected Object value(String key) {
        return map.get(key);
    }

    @Override
    protected String[] values(String key) {
        return ClassConvertUtils.convertArrays(value(key));
    }

    @Override
    public boolean exist(String key) {
        return map.containsKey(key);
    }
}
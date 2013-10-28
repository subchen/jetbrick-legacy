package jetbrick.commons.json;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.beanutils.*;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public abstract class JSON {

    public static JSONString source(String source) {
        return new JSONSource(source);
    }

    public static String toJSONString(Object object, JSONFilter... filters) {
        if (object == null) return "null";

        if (object instanceof Number) return ((Number) object).toString();
        if (object instanceof Boolean) return ((Boolean) object).toString();
        if (object instanceof String) return stringToJSONString((String) object);
        if (object instanceof StringBuffer) return stringToJSONString(object.toString());
        if (object instanceof StringBuilder) return stringToJSONString(object.toString());
        if (object instanceof Character) return stringToJSONString(object.toString());
        if (object.getClass().isArray()) return iteratorToJSONString(IteratorUtils.arrayIterator(object), filters);
        if (object instanceof Collection) return iteratorToJSONString(((Collection<?>) object).iterator(), filters);
        if (object instanceof Enumeration) return iteratorToJSONString(IteratorUtils.asIterator((Enumeration<?>) object), filters);
        if (object instanceof Iterator) return iteratorToJSONString((Iterator<?>) object, filters);
        if (object instanceof Map) return mapToJSONString((Map<?, ?>) object, filters);
        if (object instanceof DynaBean) return dynaBeanToJSONString((DynaBean) object, filters);

        for (JSONFilter filter : filters) {
            if (filter.accept(object.getClass())) {
                return filter.apply(object, filters);
            }
        }

        if (object instanceof Date) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return stringToJSONString(dateFormat.format((Date) object));
        } else if (object instanceof JSONString) {
            return ((JSONString) object).toJSONString();
        } else {
            return beanToJSONString(object, filters);
        }
    }

    private static String dynaBeanToJSONString(DynaBean bean, JSONFilter[] filters) {
        DynaProperty ps[] = bean.getDynaClass().getDynaProperties();
        Map<String, Object> map = new HashMap<String, Object>(ps.length);
        String name = null;
        for (int i = 0; i < ps.length; i++) {
            name = ps[i].getName();
            map.put(name, bean.get(name));
        }

        return mapToJSONString(map, filters);
    }

    private static String beanToJSONString(Object bean, JSONFilter[] filters) {
        return mapToJSONString(new BeanMap(bean), filters);
    }

    private static String stringToJSONString(String str) {
        return "\"" + StringEscapeUtils.escapeEcmaScript(str) + "\"";
    }

    private static String iteratorToJSONString(Iterator<?> it, JSONFilter[] filters) {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        while (it.hasNext()) {
            if (sb.length() > 1) sb.append(',');
            sb.append(toJSONString(it.next(), filters));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String mapToJSONString(Map<?, ?> map, JSONFilter[] filters) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        Iterator<?> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<?, ?> entry = (Entry<?, ?>) it.next();
            if (sb.length() > 1) sb.append(',');
            sb.append(stringToJSONString(entry.getKey().toString()));
            sb.append(':');
            sb.append(toJSONString(entry.getValue(), filters));
        }
        sb.append("}");
        return sb.toString();
    }

}

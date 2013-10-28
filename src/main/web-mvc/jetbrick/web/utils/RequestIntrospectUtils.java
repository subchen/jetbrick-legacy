package jetbrick.web.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import javax.servlet.ServletRequest;
import jetbrick.commons.bean.ClassConvertUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestIntrospectUtils {
    protected static final Logger log = LoggerFactory.getLogger(RequestIntrospectUtils.class);

    public static void introspect(Object form, ServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        Iterator<Entry<String, String[]>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String[]> entry = iter.next();
            String name = (String) entry.getKey();
            try {
                Class<?> clazz = PropertyUtils.getPropertyType(form, name);
                if (clazz == null) continue;

                String values[] = (String[]) entry.getValue();
                if (values == null) continue;

                if (clazz.isArray()) {
                    BeanUtils.setProperty(form, name, values);
                } else {
                    String value = StringUtils.trim(values[0]);
                    if (StringUtils.isEmpty(value)) {
                        if (clazz.isPrimitive() || clazz.isArray()) {
                            continue;
                        } else if (!clazz.equals(String.class)) {
                            PropertyUtils.setProperty(form, name, null);
                            continue;
                        }
                    }

                    BeanUtils.setProperty(form, name, value);
                }
            } catch (Throwable e) {
                log.warn("Can't set property for Object.", e);
            }
        }
    }

    public static void introspectFileds(Object form, ServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        Iterator<Entry<String, String[]>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String[]> entry = iter.next();
            String name = (String) entry.getKey();
            try {
                Field field = form.getClass().getField(name);

                Class<?> clazz = field.getType();
                if (clazz == null) continue;

                String values[] = (String[]) entry.getValue();
                if (values == null) continue;

                if (clazz.isArray()) {
                    Object targetValue = ClassConvertUtils.convertArrays(values, clazz.getComponentType());
                    field.set(form, targetValue);
                } else {
                    String value = StringUtils.trim(values[0]);
                    if (StringUtils.isEmpty(value)) {
                        if (clazz.isPrimitive() || clazz.isArray()) {
                            continue;
                        } else if (!clazz.equals(String.class)) {
                            field.set(form, null);
                            continue;
                        }
                    }
                    Object targetValue = ClassConvertUtils.convert(value, clazz);
                    field.set(form, targetValue);
                }
            } catch (NoSuchFieldException e) {
                continue;
            } catch (Throwable e) {
                log.warn("Can't set field value for Object", e);
            }
        }
    }

}

package jetbrick.dao.schema.validator;

import java.lang.reflect.Modifier;
import java.util.*;
import jetbrick.commons.bean.ClassFinder;
import jetbrick.commons.exception.SystemException;

@SuppressWarnings("unchecked")
public class ValidatorFactory {

    private static Map<String, Class<Validator>> maps = new HashMap<String, Class<Validator>>();

    static {
        try {
            Set<String> classNameSet = ClassFinder.getClasses(Validator.class.getPackage().getName() + ".support", false);
            for (String classname : classNameSet) {
                Class<?> clazz = Class.forName(classname);
                if (Validator.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                    register((Class<Validator>) clazz);
                }
            }
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    public static synchronized void register(Class<Validator> validatorClass) {
        try {
            Validator v = validatorClass.newInstance();
            maps.put(v.getName(), validatorClass);
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    public static synchronized Validator createValidator(String name) {
        Class<Validator> clazz = maps.get(name);
        if (clazz != null) {
            try {
                return clazz.newInstance();
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
        return null;
    }
}

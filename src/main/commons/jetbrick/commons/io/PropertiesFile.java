package jetbrick.commons.io;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import jetbrick.commons.bean.ClassConvertUtils;
import jetbrick.commons.bean.conv.MapConverter;
import jetbrick.commons.exception.SystemException;

public class PropertiesFile extends MapConverter {
    protected Properties props = new Properties();

    public PropertiesFile(Properties props) {
        this.props = props;
    }

    public PropertiesFile(File file) {
        this(file, null);
    }

    public PropertiesFile(InputStream is) {
        this(is, null);
    }

    public PropertiesFile(File file, String encoding) {
        try {
            props.load(new FileInputStream(file));
            translate(encoding);
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    public PropertiesFile(InputStream is, String encoding) {
        try {
            props.load(is);
            translate(encoding);
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    protected void translate(String encoding) {
        if (encoding == null) return;

        Enumeration<String> en = (Enumeration<String>) props.propertyNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            String value = props.getProperty(name);
            if (value == null) continue;
            try {
                value = new String(value.getBytes("ISO8859-1"), encoding);
                props.put(name, value);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
    }

    @Override
    protected Object value(String key) {
        return props.getProperty(key);
    }

    @Override
    protected String[] values(String key) {
        return ClassConvertUtils.convertArrays(value(key));
    }

    @Override
    public boolean exist(String key) {
        return props.containsKey(key);
    }

    public Properties getProperties() {
        return props;
    }

    /**
     * 读取某个前缀的子集
     */
    public PropertiesFile sub(String prefix) {
        Properties p = new Properties();
        for (Entry<?, ?> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix)) {
                key = key.substring(prefix.length());
                p.put(key, entry.getValue());
            }
        }
        return new PropertiesFile(p);
    }
}

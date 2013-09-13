package jetbrick.commons.io;

import java.io.*;
import java.util.Map.Entry;
import java.util.Properties;
import jetbrick.commons.bean.ClassConvertUtils;
import jetbrick.commons.bean.conv.MapConverter;
import jetbrick.commons.exception.SystemException;

public class PropertiesFile extends MapConverter {
    protected Properties props = new Properties();
    protected String encoding = null;

    public PropertiesFile(Properties props, String encoding) {
        this.props = props;
        this.encoding = encoding;
    }

    public PropertiesFile(File file) {
        this(file, null);
    }

    public PropertiesFile(InputStream is) {
        this(is, null);
    }

    public PropertiesFile(File file, String encoding) {
        this.encoding = encoding;

        try {
            props.load(new FileInputStream(file));
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    public PropertiesFile(InputStream is, String encoding) {
        this.encoding = encoding;

        try {
            props.load(is);
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    @Override
    protected Object value(String key) {
        String value = props.getProperty(key);
        if (value != null && encoding != null) {
            try {
                value = new String(value.getBytes("ISO8859-1"), encoding);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        }
        return value;
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
        return new PropertiesFile(p, encoding);
    }
}

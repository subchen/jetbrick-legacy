package jetbrick.commons.io;

import java.io.*;
import java.util.Properties;

/**
 * 由于 java 自带的 properties 文件格式对于多行的文本支持的不是很完善，这里以更优雅的格式进行支持。
 */
public class MultiLinesFile {
    protected Properties props = new Properties();

    public MultiLinesFile(File file, String encoding) {
        try {
            load(new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultiLinesFile(InputStream is, String encoding) {
        try {
            load(new BufferedReader(new InputStreamReader(is, encoding)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultiLinesFile(Reader reader) {
        try {
            if (!(reader instanceof BufferedReader)) {
                reader = new BufferedReader(reader);
            }
            load((BufferedReader) reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void load(BufferedReader reader) throws IOException {
        String line = null;
        String key = null;
        StringBuilder values = new StringBuilder(64);

        while ((line = reader.readLine()) != null) {
            String str = line.trim();
            if (str.startsWith("#")) {
                continue;
            }
            if (str.startsWith("[") && str.endsWith("]")) {
                if (key != null) { // save last key/value
                    props.put(key, values.toString());
                }
                key = str.substring(1, str.length() - 1).trim();
                values.setLength(0);
            } else {
                values.append(line).append("\n");
            }
        }

        if (key != null) { // save last key/value
            props.put(key, values.toString());
        }
    }

    public boolean exist(String key) {
        return props.containsKey(key);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public Properties getProperties() {
        return props;
    }
}

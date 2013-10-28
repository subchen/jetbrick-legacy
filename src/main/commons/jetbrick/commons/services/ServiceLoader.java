package jetbrick.commons.services;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class ServiceLoader {
    private static final String DEFAULT_SERVICES_FILE = "jetbrick-services.properties";
    private static final Properties props = getServiceProperties();

    /**
     * A simple service-provider loading facility.
     * 
     * <pre><code>
     * ServiceLoader.load(LogFactory.class, 
     *      "jetbrick.commons.log.Log4jLogFactory, org.apache.log4j.Logger", 
     *      "jetbrick.commons.log.Jdk14LogFactory"
     * );
     * <code></pre>
     * 
     * Load Service Sequence:
     * <ul>
     * <li>System.getProperty()</li>
     * <li>load from classpath:/humpic-services.properties</li>
     * <li>java.util.ServiceLoader.load()</li>
     * <li>defaultService if all classnames are found.</li>
     * </ul>
     * 
     * @param clazz<T>  The interface or abstract class representing the service
     * @param defaultServiceClass  A service factory class name.
     * @return A found service object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(Class<T> clazz, String... defaultServiceClass) {
        String serviceClass = System.getProperty(clazz.getName());
        if (StringUtils.isBlank(serviceClass)) {
            serviceClass = props.getProperty(clazz.getName());
        }

        if (StringUtils.isBlank(serviceClass)) {
            java.util.ServiceLoader<T> jdkLoader = java.util.ServiceLoader.load(clazz);
            Iterator<T> jdkLoadServices = jdkLoader.iterator();
            if (jdkLoadServices.hasNext()) {
                return jdkLoadServices.next();
            }
        }

        if (StringUtils.isBlank(serviceClass)) {
            if (defaultServiceClass.length == 0) return null;

            for (String className : defaultServiceClass) {
                String[] names = StringUtils.split(className, ",");
                if (names.length == 0) continue;

                boolean found = true;
                for (String name : names) {
                    try {
                        Class.forName(name.trim());
                    } catch (ClassNotFoundException e) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    serviceClass = names[0].trim();
                    break;
                }
            }
        }

        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            return (T) contextClassLoader.loadClass(serviceClass).newInstance();
        } catch (Throwable e) {
            throw SystemException.unchecked(e);
        }
    }

    private static Properties getServiceProperties() {
        Properties p = new Properties();

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        InputStream resource = contextClassLoader.getResourceAsStream(DEFAULT_SERVICES_FILE);
        if (resource != null) {
            try {
                p.load(resource);
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            } finally {
                IOUtils.closeQuietly(resource);
            }
        }
        return p;
    }

}

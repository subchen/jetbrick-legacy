package jetbrick.commons.debug;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.*;

public class Log4jConfig {

    public void resetConfiguration() {
        LogManager.resetConfiguration();

        URL config = getClass().getClassLoader().getResource("/log4j.properties");
        if (config != null) {
            PropertyConfigurator.configure(config);
        }
    }

    /**
     * @param level
     *      OFF, FATAL, ERROR, WARN, INFO, DEBUG and ALL.
     */
    public void setLevel(String logName, String level) {
        Logger log = StringUtils.isEmpty(logName) ? Logger.getRootLogger() : Logger.getLogger(logName);
        log.setLevel(Level.toLevel(level, Level.DEBUG));
    }

    @SuppressWarnings("unchecked")
    public List<Logger> getLoggers() {
        Enumeration<Logger> loggers = Logger.getRootLogger().getLoggerRepository().getCurrentLoggers();
        return EnumerationUtils.toList(loggers);
    }

}

package jetbrick.commons.debug;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.*;

public class VMs {
    public static String getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = StringUtils.substringBefore(name, "@");
        return pid;
    }

    public static String getThreadDump() {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6)) {
            String jstack = "../bin/jstack";
            if (SystemUtils.IS_OS_WINDOWS) {
                jstack = jstack + ".exe";
            }
            File jstackFile = new File(SystemUtils.JAVA_HOME, jstack);
            try {
                String command = jstackFile.getCanonicalPath() + " " + VMs.getProcessId();
                Process process = Runtime.getRuntime().exec(command);
                return IOUtils.toString(process.getInputStream(), "ISO-8859-1");
            } catch (Throwable e) {
                throw SystemException.unchecked(e);
            }
        } else {
            return "Java Version must be equal or larger than 1.6";
        }
    }

    public static boolean detectDeadlock() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadBean.findDeadlockedThreads();
        return (threadIds != null && threadIds.length > 0);
    }

    public static void main(String[] args) {
        String dump = getThreadDump();
        System.out.println(dump);
    }
}

package jetbrick.commons.lang;

import java.io.*;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ProcessUtils {

    /**
     * shell("ls -l")
     */
    public static ProcessResult shell(String command) {
        return shell(command, null, null);
    }

    public static ProcessResult shell(String command, File directory, Map<String, String> envp) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return execute(directory, envp, "cmd.exe", "/c", command);
        } else {
            return execute(directory, envp, "/bin/sh", "-c", command);
        }
    }

    /**
     * shell("ls", "-l")
     */
    public static ProcessResult execute(String... command) {
        return execute(null, null, command);
    }

    public static ProcessResult execute(File directory, Map<String, String> envp, String... command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (directory != null) {
            pb.directory(directory);
        }
        if (envp != null) {
            pb.environment().putAll(envp);
        }

        ProcessResult result = new ProcessResult();

        Process p = null;
        try {
            p = pb.start();
            new InputStreamReadThread("shell-exec-stdout", p.getInputStream(), result.stdout).start();
            new InputStreamReadThread("shell-exec-stderr", p.getErrorStream(), result.stderr).start();
            p.waitFor();
            result.exitValue = p.exitValue();
        } catch (Throwable e) {
            result.error = e;
        } finally {
            if (p != null) {
                p.destroy();
            }
        }
        return result;
    }

    static class ProcessResult {
        int exitValue = -99;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        Throwable error;

        public boolean good() {
            return error != null && exitValue == 0;
        }

        public int exitValue() {
            return exitValue;
        }

        public String stdout() {
            return stdout.toString();
        }

        public String stdout(String charset) {
            try {
                return stdout.toString(charset);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public String stderr() {
            return stderr.toString();
        }

        public String stderr(String charset) {
            try {
                return stderr.toString(charset);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public Throwable error() {
            return error;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
    }

    static class InputStreamReadThread extends Thread {
        final InputStream is;
        final OutputStream os;

        InputStreamReadThread(String name, InputStream is, OutputStream os) {
            super(name);
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            try {
                int n = is.read();
                while (n > -1) {
                    os.write(n);
                    n = is.read();
                }
            } catch (Throwable e) {
                // hit stream eof, do nothing
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }
}

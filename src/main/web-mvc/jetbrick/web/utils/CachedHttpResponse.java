package jetbrick.web.utils;

import java.io.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CachedHttpResponse extends HttpServletResponseWrapper {
    private ByteArrayOutputStream os;
    private PrintWriter writer;
    private ByteArrayServletOutputStream output;
    private String encoding;

    public CachedHttpResponse(HttpServletResponse response, String encoding) {
        super(response);

        this.os = new ByteArrayOutputStream(4096);
        this.encoding = encoding;
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(os, encoding));
        }
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (output == null) {
            output = new ByteArrayServletOutputStream(os);
        }
        return output;
    }

    public String getContent() {
        try {
            return os.toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        os.close();
        if (writer != null) {
            writer.close();
        }
        if (output != null) {
            output.close();
        }
    }

    static class ByteArrayServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream os;

        public ByteArrayServletOutputStream(ByteArrayOutputStream os) {
            this.os = os;
        }

        @Override
        public void write(int i) throws IOException {
            os.write(i);
        }
    }
}

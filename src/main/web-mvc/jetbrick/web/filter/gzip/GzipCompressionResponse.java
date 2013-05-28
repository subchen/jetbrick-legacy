package jetbrick.web.filter.gzip;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class GzipCompressionResponse extends HttpServletResponseWrapper {
    private ServletOutputStream out;
    private GzipCompressedStream compressedOut;
    private PrintWriter writer;

    public GzipCompressionResponse(HttpServletResponse response) throws IOException {
        super(response);
        compressedOut = new GzipCompressedStream(response.getOutputStream());
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (out == null) {
            if (writer != null) throw new IllegalStateException("getWriter() has already been called on this response.");
            out = compressedOut;
        }
        return out;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            if (out != null) throw new IllegalStateException("getOutputStream() has already been called on this response.");
            writer = new PrintWriter(compressedOut);
        }
        return writer;
    }

    @Override
    public void flushBuffer() {
        try {
            if (writer != null)
                writer.flush();
            else if (out != null) out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        super.reset();
        try {
            compressedOut.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resetBuffer() {
        super.resetBuffer();
        try {
            compressedOut.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void finish() throws IOException {
        compressedOut.close();
    }
}

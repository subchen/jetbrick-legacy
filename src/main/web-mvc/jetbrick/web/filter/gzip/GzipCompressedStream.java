package jetbrick.web.filter.gzip;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletOutputStream;

public class GzipCompressedStream extends ServletOutputStream {
    private ServletOutputStream out;
    private GZIPOutputStream gzip;

    public GzipCompressedStream(ServletOutputStream out) throws IOException {
        this.out = out;
        reset();
    }

    @Override
    public void close() throws IOException {
        gzip.close();
    }

    @Override
    public void flush() throws IOException {
        gzip.flush();
    }

    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        gzip.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        gzip.write(b);
    }

    public void reset() throws IOException {
        gzip = new GZIPOutputStream(out);
    }
}

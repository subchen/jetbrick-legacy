package jetbrick.commons.io;

import java.io.*;
import java.util.zip.*;

public class CompressUtils {

    public static byte[] compressGZIP(byte bytes[]) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream gzipos = new GZIPOutputStream(os);
        gzipos.write(bytes, 0, bytes.length);
        gzipos.close();
        return os.toByteArray();
    }

    public static byte[] decompressGZIP(byte bytes[]) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipis = new GZIPInputStream(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int i;
        while ((i = gzipis.read()) != -1) {
            os.write(i);
        }
        gzipis.close();
        os.close();
        return os.toByteArray();
    }

    public static byte[] compressZip(byte bytes[]) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zipos = new ZipOutputStream(os);
        zipos.putNextEntry(new ZipEntry("ZIP"));
        zipos.write(bytes, 0, bytes.length);
        zipos.close();
        return os.toByteArray();
    }

    public static byte[] decompressZip(byte bytes[]) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        ZipInputStream zipis = new ZipInputStream(is);
        zipis.getNextEntry();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int i;
        while ((i = zipis.read()) != -1) {
            os.write(i);
        }
        zipis.close();
        os.close();
        return os.toByteArray();
    }

}

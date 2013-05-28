package jetbrick.commons.io;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

public class FileMonitorUtils {

    // isChanged 方法的数据缓存 Map
    private static Map<File, Long> changedFilesMap = new Hashtable<File, Long>(10);

    /**
     * 判断一个文件是否被改变. (第一次调用将返回 true)
     */
    public static boolean isChanged(File filename) {
        Long lastModified = changedFilesMap.get(filename);
        if (lastModified == null) {
            changedFilesMap.put(filename, filename.lastModified());
            return true;
        } else {
            if (lastModified.longValue() != filename.lastModified()) {
                changedFilesMap.put(filename, filename.lastModified());
                return true;
            }
        }
        return false;
    }
}

package jetbrick.dao.orm;

import java.util.regex.Pattern;

public class SqlUtils {

    // 从一个标准的select语句中，生成一个 select count(*) 语句
    public static String get_sql_select_count(String sql) {
        String count_sql = sql.replaceAll("\\s+", " ");
        int pos = count_sql.toLowerCase().indexOf(" from ");
        count_sql = count_sql.substring(pos);

        pos = count_sql.toLowerCase().lastIndexOf(" order by ");
        int lastpos = count_sql.toLowerCase().lastIndexOf(")");
        if (pos != -1 && pos > lastpos) {
            count_sql = count_sql.substring(0, pos);
        }

        String regex = "(left|right|inner) join (fetch )?\\w+(\\.\\w+)*";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        count_sql = p.matcher(count_sql).replaceAll("");

        count_sql = "select count(*) " + count_sql;
        return count_sql;
    }
}

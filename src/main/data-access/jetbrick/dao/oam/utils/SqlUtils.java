package jetbrick.dao.oam.utils;

import java.util.regex.Pattern;

public class SqlUtils {

	// 从一个标准的select语句中，生成一个 select count(*) 语句
	public static String sql_get_count(String sql) {
		String sql_count = sql.replaceAll("[\\n|\\r|\\t]", " ");
		int pos = sql_count.toLowerCase().indexOf(" from ");
		sql_count = sql_count.substring(pos);

		pos = sql_count.toLowerCase().lastIndexOf(" order by ");
		int lastpos = sql_count.toLowerCase().lastIndexOf(")");
		if (pos != -1 && pos > lastpos) {
			sql_count = sql_count.substring(0, pos);
		}

		String regex = "((left|right|inner)\\s+)?+join\\s+(fetch\\s+)?\\w+(\\.\\w+)*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		sql_count = p.matcher(sql_count).replaceAll("");

		sql_count = "select count(*) " + sql_count;
		return sql_count;
	}
}

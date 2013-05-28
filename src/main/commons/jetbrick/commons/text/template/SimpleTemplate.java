package jetbrick.commons.text.template;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrick.commons.exception.SystemException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;

public class SimpleTemplate implements Template {
	protected static final Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");

	@Override
	public String render(String source, Map<String, Object> context) {
		Matcher m = pattern.matcher(source);
		StringBuffer sb = new StringBuffer();
		try {
			while (m.find()) {
				String name = m.group(1);
				Object value = PropertyUtils.getNestedProperty(context, name);
				m.appendReplacement(sb, ObjectUtils.toString(value));
			}
			m.appendTail(sb);
		} catch (Exception e) {
			throw SystemException.unchecked(e);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String source = "hello {world}, my age is {user.age}\n\n my hobby-1: {user.hobbies.[0]}";
		Map<String, Object> user = new HashMap<String, Object>();
		user.put("age", 20);
		user.put("hobbies", Arrays.asList("aa", "bb", "cc").toArray());

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("world", "haha");
		context.put("user", user);
		String s = new SimpleTemplate().render(source, context);
		System.out.println(s);
	}
}

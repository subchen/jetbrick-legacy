package jetbrick.commons.text.template;

import java.util.Map;

public interface Template {

    String render(String source, Map<String, Object> context);

}

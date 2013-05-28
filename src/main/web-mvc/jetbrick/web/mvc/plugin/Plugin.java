package jetbrick.web.mvc.plugin;

import jetbrick.web.mvc.config.WebappConfig;

public interface Plugin {

    public void init(WebappConfig config);

    public void destory();

}

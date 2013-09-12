package jetbrick.web.mvc.view;

import jetbrick.web.mvc.ViewRender;
import jetbrick.web.mvc.config.WebappConfig;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractViewRender implements ViewRender {
    protected final String encoding;
    protected String viewPattern;
    protected String layoutKey = "layout";
    protected String mainContentKey = "mainContent";

    public AbstractViewRender() {
        encoding = WebappConfig.getInstance().getEncoding();
    }

    public void setViewPattern(String viewPattern) {
        this.viewPattern = viewPattern;
    }

    public void setLayoutKey(String layoutKey) {
        this.layoutKey = layoutKey;
    }

    public void setMainContentKey(String mainContentKey) {
        this.mainContentKey = mainContentKey;
    }

    @Override
    public String getDefaultViewName(String viewName) {
        viewName = StringUtils.replace(viewPattern, "*", viewName);
        return viewName;
    }
}

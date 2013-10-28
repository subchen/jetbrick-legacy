package jetbrick.dao.orm;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import jetbrick.web.utils.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;

public class Pagelist<T> implements JSONAware {
    private List<T> items;
    private int page;
    private int pageSize;
    private int count;
    private String pageUrl;

    public Pagelist() {
        page = 1;
        pageSize = 20;
        count = -1;
        items = Collections.emptyList();
    }

    public Pagelist(HttpServletRequest request) {
        page = getParameterAsInteger(request, "page", 1);
        pageSize = getParameterAsInteger(request, "pageSize", 20);
        count = getParameterAsInteger(request, "count", -1);

        pageUrl = ServletUtils.getUrlParameters(request, "page");
        if (pageUrl.indexOf("m=pagelist") < 0) {
            pageUrl = pageUrl + (pageUrl.indexOf('?') < 0 ? '?' : '&') + "m=pagelist";
        }

        items = Collections.emptyList();
    }

    private int getParameterAsInteger(HttpServletRequest request, String key, int defaultValue) {
        String value = request.getParameter(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public int getPageCount() {
        if (count > 0) {
            return (count - 1) / pageSize + 1;
        } else {
            return 1;
        }
    }

    public int getFirstResult() {
        return (page - 1) * pageSize;
    }

    public boolean isEmpty() {
        return count > 0;
    }

    public boolean isFirstPage() {
        return page == 1;
    }

    public boolean isLastPage() {
        return page == getPageCount();
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("page", page);
        json.put("pageSize", pageSize);
        json.put("pageCount", getPageCount());
        json.put("items", items);
        json.put("count", count);
        json.put("pageUrl", pageUrl);
        return json;
    }

    @Override
    public String toJSONString() {
        return toJSONObject().toString();
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }
}

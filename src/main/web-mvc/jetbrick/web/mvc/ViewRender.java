package jetbrick.web.mvc;

public interface ViewRender {

    public void render(RequestContext rc, String view) throws Throwable;

    public String getDefaultViewName(String viewName);

}

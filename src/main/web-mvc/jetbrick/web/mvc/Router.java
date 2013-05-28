package jetbrick.web.mvc;

import javax.servlet.http.HttpServletRequest;

public interface Router {

	public boolean accept(HttpServletRequest request);
	
    public RouteInfo getRouteInfo(HttpServletRequest request);

}

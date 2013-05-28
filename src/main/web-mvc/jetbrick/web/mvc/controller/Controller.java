package jetbrick.web.mvc.controller;

import jetbrick.web.mvc.RequestContext;
import jetbrick.web.mvc.Result;

/**
 * 配合ControllerEnhancer使用
 */
public interface Controller {

    public Result __main__(RequestContext ac) throws Throwable;

}

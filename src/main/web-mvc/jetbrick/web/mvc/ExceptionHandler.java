package jetbrick.web.mvc;

public interface ExceptionHandler {

    public void handleError(Throwable e) throws Throwable;

}

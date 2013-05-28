package jetbrick.commons.exception;

public enum SecurityError implements ErrorCode {
    //@formatter:off

    INVALID_USERNAME,
    INVALID_PASSWORD,
    
    NOT_LOGGED_IN,
    PERMISSION_DENIED,
    
    //@formatter:on
}

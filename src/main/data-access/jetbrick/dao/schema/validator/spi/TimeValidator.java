package jetbrick.dao.schema.validator.spi;

import jetbrick.commons.lang.DateUtils;

public class TimeValidator extends DateTimeValidator {

    private static final TimeValidator instance = new TimeValidator();

    public static TimeValidator getInstance() {
        return instance;
    }

    public TimeValidator() {
        super(DateUtils.FORMAT_TIME);
    }
}

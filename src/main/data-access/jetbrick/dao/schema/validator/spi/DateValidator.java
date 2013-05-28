package jetbrick.dao.schema.validator.spi;

import jetbrick.commons.lang.DateUtils;

public class DateValidator extends DateTimeValidator {

    private static final DateValidator instance = new DateValidator();

    public static DateValidator getInstance() {
        return instance;
    }

    public DateValidator() {
        super(DateUtils.FORMAT_DATE);
    }
}

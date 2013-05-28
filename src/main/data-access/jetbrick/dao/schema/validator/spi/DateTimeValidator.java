package jetbrick.dao.schema.validator.spi;

import java.util.Calendar;
import java.util.Date;
import jetbrick.commons.lang.DateUtils;
import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class DateTimeValidator extends Validator {

    private static final DateTimeValidator instance = new DateTimeValidator();

    public static DateTimeValidator getInstance() {
        return instance;
    }

    private String format;

    public DateTimeValidator() {
        this(DateUtils.FORMAT_DATE_TIME);
    }

    public DateTimeValidator(String format) {
        this.format = format;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (value instanceof Date || value instanceof Calendar) {
            return;
        }
        if (!(value instanceof String)) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        try {
            Date d = DateUtils.parse(value.toString(), format);
            if (d == null) {
                throw new Exception();
            }
        } catch (Throwable e) {
            throw new ValidatorException("Invalid format for %s. expected: %s, was: %s.", name, format, value);
        }
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}

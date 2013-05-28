package jetbrick.dao.schema.validator;

@SuppressWarnings("serial")
public class ValidatorException extends RuntimeException {

    public ValidatorException(String format, Object... args) {
        super(String.format(format, args));
    }

}

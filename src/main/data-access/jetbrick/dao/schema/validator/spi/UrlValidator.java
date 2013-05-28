package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class UrlValidator extends Validator {

    private static final String[] DEFAULT_SCHEMES = new String[] { "http", "https", "ftp" };
    private static final UrlValidator instance = new UrlValidator();

    public static UrlValidator getInstance() {
        return instance;
    }

    private String[] schemes;

    public UrlValidator() {
        this(DEFAULT_SCHEMES);
    }

    public UrlValidator(String[] schemes) {
        this.schemes = schemes;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof String)) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        org.apache.commons.validator.UrlValidator v = new org.apache.commons.validator.UrlValidator(getSchemes());
        if (v.isValid(value.toString())) {
            throw new ValidatorException("Invalid url format for %s: %s. The URL must be start with http(s)://.", name, value);
        }
    }

    public String[] getSchemes() {
        return schemes;
    }

    public void setSchemes(String[] schemes) {
        this.schemes = schemes;
    }

}

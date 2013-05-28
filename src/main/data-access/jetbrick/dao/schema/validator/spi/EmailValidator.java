package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class EmailValidator extends Validator {

    private static final EmailValidator instance = new EmailValidator();

    public static EmailValidator getInstance() {
        return instance;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof String)) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        boolean valid = org.apache.commons.validator.EmailValidator.getInstance().isValid(value.toString());
        if (!valid) {
            throw new ValidatorException("Invalid email format for %s: %s.", name, value);
        }
    }
}

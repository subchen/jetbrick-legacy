package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class RequiredValidator extends Validator {

    private static final RequiredValidator instance = new RequiredValidator();

    public static final RequiredValidator getInstance() {
        return instance;
    }

    @Override
    public void validate(String name, Object value) throws ValidatorException {
        if (value == null) {
            throw new ValidatorException("%s must be not empty.", name);
        }
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        return;
    }
}

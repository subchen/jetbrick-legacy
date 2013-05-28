package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;
import org.apache.commons.lang3.StringUtils;

public class TelephoneValidator extends Validator {

    private static final TelephoneValidator instance = new TelephoneValidator();

    public static TelephoneValidator getInstance() {
        return instance;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof String)) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        String telephone = value.toString();
        if (telephone.length() < 7) {
            throw new ValidatorException("Invalid telephone format for %s: %s.", name, value);
        }
        String[] parts = StringUtils.split(telephone, "-");
        if (parts.length > 4) {
            throw new ValidatorException("Invalid telephone format for %s: %s.", name, value);
        }
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].length() < 2 || !parts[i].matches("\\d+")) {
                throw new ValidatorException("Invalid telephone format for %s: %s.", name, value);
            }
        }
    }

}

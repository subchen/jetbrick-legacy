package jetbrick.dao.schema.validator.spi;

import jetbrick.commons.lang.WildcharUtils;
import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class WildcharMatchValidator extends Validator {

    private String expression;

    public WildcharMatchValidator() {
    }

    public WildcharMatchValidator(String expression) {
        this.expression = expression;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof String)) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        if (!WildcharUtils.wildcharMatch(value.toString(), expression)) {
            throw new ValidatorException("Invalid format for %s: %s", name, expression);
        }
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

}

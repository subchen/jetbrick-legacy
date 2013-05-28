package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class RegexValidator extends Validator {

    private String expression;

    public RegexValidator() {
    }

    public RegexValidator(String expression) {
        this.expression = expression;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof String)) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        if (!value.toString().matches(expression)) {
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

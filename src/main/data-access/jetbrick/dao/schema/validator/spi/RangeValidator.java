package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class RangeValidator extends Validator {
    private Number min;
    private Number max;

    public RangeValidator() {
    }

    public RangeValidator(Number min, Number max) {
        this.min = min;
        this.max = max;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof Number)) {
            throw new ValidatorException("Invalid type for %s. expected: Number, was: %s.", name, value.getClass().getName());
        }

        if (min == null && max == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Comparable<Number> val = (Comparable<Number>) value;
        if (min != null && val.compareTo(min) < 0) {
            throw new ValidatorException("%s must be in the range of (%s): %s.", name, getValidRange(), value.toString());
        }
        if (max != null && val.compareTo(max) > 0) {
            throw new ValidatorException("%s must be in the range of (%s): %s.", name, getValidRange(), value.toString());
        }
    }

    private String getValidRange() {
        if (min == null) {
            return "<=" + max;
        } else if (max == null) {
            return ">=" + min;
        } else if (min == max) {
            return "=" + min;
        } else {
            return min + "-" + max;
        }
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }
}

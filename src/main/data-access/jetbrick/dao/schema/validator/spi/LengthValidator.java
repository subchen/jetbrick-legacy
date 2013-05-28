package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;
import org.apache.commons.lang3.ObjectUtils;

public class LengthValidator extends Validator {
    private Integer min;
    private Integer max;

    public LengthValidator() {
    }

    public LengthValidator(Integer min, Integer max) {
        this.min = min;
        this.max = max;

        if (this.min == null) {
            this.min = 0;
        }
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        if (!(value instanceof String) && !(value instanceof byte[])) {
            throw new ValidatorException("Invalid type for %s. expected: String, was: %s.", name, value.getClass().getName());
        }

        if (max == null) {
            return;
        }

        int length = getBytesLength(value);
        if (length < min) {
            throw new ValidatorException("The length of %s is too short (%s): %s.", name, getValidRange(), value);
        }
        if (max != null && length > max) {
            throw new ValidatorException("The length of %s is too long (%s): %s.", name, getValidRange(), value);
        }
    }

    private String getValidRange() {
        if (max == null) {
            return ">=" + min;
        } else if (ObjectUtils.equals(min, max)) {
            return "=" + max;
        } else {
            return min + "-" + max;
        }
    }

    private int getBytesLength(Object value) {
        if (value instanceof String) {
            return value.toString().length();
        } else if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        return -1;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

}

package jetbrick.dao.schema.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class Validator {

    public String getName() {
        String name = getClass().getSimpleName();
        name = StringUtils.removeEnd(name, "Validator");
        return name.toLowerCase();
    }

    public boolean isValid(Object value) {
        try {
            validate(value);
            return true;
        } catch (ValidatorException e) {
            return false;
        }
    }

    public void validate(Object value) throws ValidatorException {
        validate("{field}", value);
    }

    public void validate(String name, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        doValidate(name, value);
    }

    protected abstract void doValidate(String name, Object value) throws ValidatorException;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

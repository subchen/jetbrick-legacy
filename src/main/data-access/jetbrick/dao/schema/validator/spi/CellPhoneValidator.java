package jetbrick.dao.schema.validator.spi;

public class CellPhoneValidator extends RegexValidator {

    private static final CellPhoneValidator instance = new CellPhoneValidator();

    public static CellPhoneValidator getInstance() {
        return instance;
    }

    public CellPhoneValidator() {
        super("^1[358][0-9]{9}$");
    }

}

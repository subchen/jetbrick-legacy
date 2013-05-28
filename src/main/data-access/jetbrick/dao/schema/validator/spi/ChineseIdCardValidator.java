package jetbrick.dao.schema.validator.spi;

import jetbrick.dao.schema.validator.Validator;
import jetbrick.dao.schema.validator.ValidatorException;

public class ChineseIdCardValidator extends Validator {

    private static final ChineseIdCardValidator instance = new ChineseIdCardValidator();

    public static ChineseIdCardValidator getInstance() {
        return instance;
    }

    @Override
    protected void doValidate(String name, Object value) throws ValidatorException {
        boolean verify = ChineseIdCard.instance.verify(value.toString());
        if (!verify) {
            throw new ValidatorException("Invalid ID card format for %s: %s.", name, value);
        }
    }

    static class ChineseIdCard {
        static ChineseIdCard instance = new ChineseIdCard();

        // wi =2(n-1)(mod 11)
        final static int[] wi = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 };
        // verify digit
        final static int[] vi = { 1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2 };

        private int[] ai = new int[18];

        public ChineseIdCard() {
        }

        // verify
        public boolean verify(String idCard) {
            if (idCard.length() == 15) {
                idCard = uptoEighteen(idCard);
            }
            if (idCard.length() != 18) {
                return false;
            }
            String verify = idCard.substring(17, 18);
            if (verify.equals(getChecksum(idCard))) {
                return true;
            }
            return false;
        }

        public String getChecksum(String eighteenIdCard) {
            int remaining = 0;
            if (eighteenIdCard.length() == 18) {
                eighteenIdCard = eighteenIdCard.substring(0, 17);
            }
            if (eighteenIdCard.length() == 17) {
                int sum = 0;
                for (int i = 0; i < 17; i++) {
                    String k = eighteenIdCard.substring(i, i + 1);
                    ai[i] = Integer.valueOf(k);
                }
                for (int i = 0; i < 17; i++) {
                    sum = sum + wi[i] * ai[i];
                }
                remaining = sum % 11;
            }
            return remaining == 2 ? "X" : String.valueOf(vi[remaining]);
        }

        // 15 update to 18
        public String uptoEighteen(String fifteenIdCard) {
            String eighteenIdCard = fifteenIdCard.substring(0, 6);
            eighteenIdCard = eighteenIdCard + "19";
            eighteenIdCard = eighteenIdCard + fifteenIdCard.substring(6, 15);
            eighteenIdCard = eighteenIdCard + getChecksum(eighteenIdCard);
            return eighteenIdCard;
        }
    }

}

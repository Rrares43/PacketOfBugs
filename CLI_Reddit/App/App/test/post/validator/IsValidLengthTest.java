package post.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IsValidLengthTest {

    @Test
    void shouldReturnTrueWhenStringIsWithinOrAtMaxLength() {
        IsValidLength validator = new IsValidLength(5);

        assertTrue(validator.isValid("val"));
        assertTrue(validator.isValid("valid"));
        assertTrue(validator.isValid(""));
        assertTrue(validator.isValid("!@#$%"));
    }

    @Test
    void shouldReturnFalseWhenStringExceedsMaxLengthOrIsNull() {
        IsValidLength validator = new IsValidLength(5);

        assertFalse(validator.isValid("toolong"));
        assertFalse(validator.isValid(null));
    }

    @Test
    void shouldHandleEdgeCaseLimitsForMaxLength() {
        IsValidLength zeroLimitValidator = new IsValidLength(0);
        assertTrue(zeroLimitValidator.isValid(""));
        assertFalse(zeroLimitValidator.isValid("a"));

        IsValidLength negativeLimitValidator = new IsValidLength(-1);
        assertFalse(negativeLimitValidator.isValid("any"));
    }
}
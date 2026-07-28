package post.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IsNotBlankTest {

    @Test
    void shouldReturnTrueForValidStrings() {
        IsNotBlank validator = new IsNotBlank();

        assertTrue(validator.isValid("Valid string"));
        assertTrue(validator.isValid("  valid"));
        assertTrue(validator.isValid("valid  "));
        assertTrue(validator.isValid("a"));
        assertTrue(validator.isValid("123"));
        assertTrue(validator.isValid("!@#$%"));
    }

    @Test
    void shouldReturnFalseForNullEmptyOrWhitespaceStrings() {
        IsNotBlank validator = new IsNotBlank();

        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("   "));
        assertFalse(validator.isValid("\t\t"));
        assertFalse(validator.isValid("\n\n"));
        assertFalse(validator.isValid(" \t\n "));
    }
}
package account.verification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordVerificationTest {

    @Test
    void testValidPassword() {
        String validPassword = "ValidPass123!";
        assertTrue(PasswordVerification.verify(validPassword));
    }

    @Test
    void testPasswordTooShort() {
        String shortPassword = "Short1!";
        assertFalse(PasswordVerification.verify(shortPassword));
    }

    @Test
    void testNullPassword() {
        assertFalse(PasswordVerification.verify(null));
    }

    @Test
    void testPasswordExactlyEightCharacters() {
        String exactlyEight = "Valid1!@";
        assertTrue(PasswordVerification.verify(exactlyEight));
    }

    @Test
    void testPasswordSimple() {
        String simplePassword = "abcdefgh";
        assertTrue(PasswordVerification.verify(simplePassword));
    }
}

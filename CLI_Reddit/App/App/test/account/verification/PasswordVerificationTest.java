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
    void testPasswordMissingSpecialCharacters() {
        String noSpecialPassword = "NoSpecialChars123";
        assertFalse(PasswordVerification.verify(noSpecialPassword));
    }

    @Test
    void testNullPassword() {
        assertFalse(PasswordVerification.verify(null));
    }

    @Test
    void testPasswordMissingUppercase() {
        String noUppercase = "nouppercase123!";
        assertFalse(PasswordVerification.verify(noUppercase));
    }

    @Test
    void testPasswordMissingLowercase() {
        String noLowercase = "NOLOWERCASE123!";
        assertFalse(PasswordVerification.verify(noLowercase));
    }

    @Test
    void testPasswordMissingNumber() {
        String noNumber = "NoNumberHere!";
        assertFalse(PasswordVerification.verify(noNumber));
    }

    @Test
    void testPasswordWithWhitespace() {
        String withWhitespace = "Valid Pass123!";
        assertFalse(PasswordVerification.verify(withWhitespace));
    }

    @Test
    void testPasswordTooLong() {
        String longPassword = "ThisPasswordIsWayTooLongAndExceedsTheMaximumAllowedLengthOfFortyCharacters123!";
        assertFalse(PasswordVerification.verify(longPassword));
    }

    @Test
    void testPasswordExactlyEightCharacters() {
        String exactlyEight = "Valid1!";
        assertFalse(PasswordVerification.verify(exactlyEight));
    }

    @Test
    void testPasswordExactlyEightCharactersValid() {
        String exactlyEightValid = "Valid1!@";
        assertTrue(PasswordVerification.verify(exactlyEightValid));
    }
}

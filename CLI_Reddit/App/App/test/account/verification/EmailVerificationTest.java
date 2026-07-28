package account.verification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationTest {

    @Test
    void testValidEmail() {
        String validEmail = "user@example.com";
        assertTrue(EmailVerification.verify(validEmail));
    }

    @Test
    void testValidEmailWithSubdomain() {
        String validEmail = "user@mail.example.com";
        assertTrue(EmailVerification.verify(validEmail));
    }

    @Test
    void testNullEmail() {
        assertFalse(EmailVerification.verify(null));
    }

    @Test
    void testBlankEmail() {
        String blankEmail = "";
        assertFalse(EmailVerification.verify(blankEmail));
    }

    @Test
    void testWhitespaceEmail() {
        String whitespaceEmail = "   ";
        assertFalse(EmailVerification.verify(whitespaceEmail));
    }

    @Test
    void testEmailMissingAtSymbol() {
        String noAtEmail = "userexample.com";
        assertFalse(EmailVerification.verify(noAtEmail));
    }

    @Test
    void testEmailMissingDot() {
        String noDotEmail = "user@examplecom";
        assertFalse(EmailVerification.verify(noDotEmail));
    }

    @Test
    void testEmailDotImmediatelyAfterAt() {
        String dotAfterAt = "user@.com";
        assertFalse(EmailVerification.verify(dotAfterAt));
    }

    @Test
    void testEmailMultipleDots() {
        String multipleDots = "user@example.co.uk";
        assertTrue(EmailVerification.verify(multipleDots));
    }

    @Test
    void testEmailWithNumbers() {
        String emailWithNumbers = "user123@example123.com";
        assertTrue(EmailVerification.verify(emailWithNumbers));
    }

    @Test
    void testEmailWithPlus() {
        String emailWithPlus = "user+tag@example.com";
        assertTrue(EmailVerification.verify(emailWithPlus));
    }

    @Test
    void testEmailWithUnderscore() {
        String emailWithUnderscore = "user_name@example.com";
        assertTrue(EmailVerification.verify(emailWithUnderscore));
    }

}

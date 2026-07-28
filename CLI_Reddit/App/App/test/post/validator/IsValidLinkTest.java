package post.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IsValidLinkTest {

    @Test
    void shouldReturnTrueForValidLinks() {
        IsValidLink validator = new IsValidLink();

        assertTrue(validator.isValid("http://example.com"));
        assertTrue(validator.isValid("https://example.com"));
        assertTrue(validator.isValid("www.example.com"));
        assertTrue(validator.isValid("https://www.example.com"));
        assertTrue(validator.isValid("http://www.example.com"));
        assertTrue(validator.isValid("https://sub.example.com"));
        assertTrue(validator.isValid("https://example.co.uk"));
        assertTrue(validator.isValid("https://my-example123.com"));
        assertTrue(validator.isValid("https://my_example.com"));
        assertTrue(validator.isValid("https://example.com:8080"));
        assertTrue(validator.isValid("https://example.com/path/to/resource"));
        assertTrue(validator.isValid("https://example.com/"));
        assertTrue(validator.isValid("https://example.com?param=value"));
        assertTrue(validator.isValid("https://example.com#section"));
    }

    @Test
    void shouldReturnFalseForInvalidOrMalformedLinks() {
        IsValidLink validator = new IsValidLink();

        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("   "));
        assertFalse(validator.isValid("example.com"));
        assertFalse(validator.isValid("http://example"));
        assertFalse(validator.isValid("http://"));
        assertFalse(validator.isValid("ftp://example.com"));
        assertFalse(validator.isValid("http ://example.com"));
        assertFalse(validator.isValid("http://example.com/path with spaces"));
    }
}
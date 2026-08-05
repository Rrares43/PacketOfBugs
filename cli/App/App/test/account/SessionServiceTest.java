package account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void testInitialGuestUser() {
        SessionService sessionService = new SessionService();
        assertEquals("guest", sessionService.getCurrentUsername());
        assertFalse(sessionService.isLoggedIn());
    }

    @Test
    void testSuccessfulLogin() {
        SessionService sessionService = new SessionService();
        sessionService.login("testuser");

        assertEquals("testuser", sessionService.getCurrentUsername());
        assertTrue(sessionService.isLoggedIn());
    }

    @Test
    void testMultipleLoginsOverwritesPreviousUser() {
        SessionService sessionService = new SessionService();
        sessionService.login("user1");
        sessionService.login("user2");

        assertEquals("user2", sessionService.getCurrentUsername());
        assertTrue(sessionService.isLoggedIn());
    }

}
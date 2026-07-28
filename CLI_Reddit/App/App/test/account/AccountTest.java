package account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testAccountConstructor() {
        Account account = new Account("testuser", "test@example.com", "Password123!");
        assertEquals("testuser", account.getUsername());
        assertEquals("test@example.com", account.getEmail());
        assertEquals("Password123!", account.getPassword());
    }

    @Test
    void testSetUsername() {
        Account account = new Account("olduser", "test@example.com", "Password123!");
        account.setUsername("newuser");
        assertEquals("newuser", account.getUsername());
    }

    @Test
    void testSetEmail() {
        Account account = new Account("testuser", "old@example.com", "Password123!");
        account.setEmail("new@example.com");
        assertEquals("new@example.com", account.getEmail());
    }

    @Test
    void testSetPassword() {
        Account account = new Account("testuser", "test@example.com", "OldPass123!");
        account.setPassword("NewPass123!");
        assertEquals("NewPass123!", account.getPassword());
    }

}

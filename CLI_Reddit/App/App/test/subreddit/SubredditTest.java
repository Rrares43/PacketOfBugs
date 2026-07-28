package subreddit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubredditTest {

    @Test
    void shouldAddPrefixWhenNameDoesNotHaveIt() {
        Subreddit subreddit = new Subreddit("java", "Java Community", "admin");

        assertEquals("r/java", subreddit.getName());
        assertEquals("Java Community", subreddit.getDescription());
        assertEquals("admin", subreddit.getOwner());
    }

    @Test
    void shouldKeepPrefixIfAlreadyPresent() {
        Subreddit subreddit = new Subreddit("r/java", "Java Community", "admin");

        assertEquals("r/java", subreddit.getName());
    }

    @Test
    void shouldEnsurePrefixWhenSettingNewName() {
        Subreddit subreddit = new Subreddit("oldname", "Description", "admin");

        subreddit.setName("newname");

        assertEquals("r/newname", subreddit.getName());
    }

    @Test
    void shouldHandleSpecialCharactersAndSpacesInName() {
        Subreddit sub1 = new Subreddit("test-123_community", "Desc", "owner");
        Subreddit sub2 = new Subreddit("learn java", "Desc", "owner");

        assertEquals("r/test-123_community", sub1.getName());
        assertEquals("r/learn java", sub2.getName());
    }

}
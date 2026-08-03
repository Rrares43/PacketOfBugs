package post.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void shouldCreateCommentWithInitialDefaults() {
        Comment comment = new Comment(1, "Test comment", "testuser");

        assertEquals(1, comment.getId());
        assertEquals("Test comment", comment.getText());
        assertEquals("testuser", comment.getAuthor());
        assertNotNull(comment.getReplies());
        assertTrue(comment.getReplies().isEmpty());
        assertEquals(0, comment.getUpvotes());
        assertEquals(0, comment.getDownvotes());
    }

    @Test
    void shouldAddAndMaintainMultipleReplies() {
        Comment parent = new Comment(1, "Parent comment", "testuser");
        Comment reply1 = new Comment(2, "Reply 1", "user1");
        Comment reply2 = new Comment(3, "Reply 2", "user2");

        parent.addReply(reply1);
        parent.addReply(reply2);

        List<Comment> replies = parent.getReplies();
        assertEquals(2, replies.size());
        assertEquals("Reply 1", replies.get(0).getText());
        assertEquals("Reply 2", replies.get(1).getText());
    }

    @Test
    void shouldSupportNestedRepliesTree() {
        Comment parent = new Comment(1, "Parent", "user1");
        Comment reply = new Comment(2, "Reply", "user2");
        Comment nestedReply = new Comment(3, "Nested", "user3");

        parent.addReply(reply);
        reply.addReply(nestedReply);

        assertEquals(1, parent.getReplies().size());
        assertEquals(1, parent.getReplies().get(0).getReplies().size());
        assertEquals("Nested", parent.getReplies().get(0).getReplies().get(0).getText());
    }

    @Test
    void shouldUpdateVoteCounts() {
        Comment comment = new Comment(1, "Test comment", "testuser");

        comment.setVoteCounts(1, 1);

        assertEquals(1, comment.getUpvotes());
        assertEquals(1, comment.getDownvotes());
    }

    @Test
    void shouldExposeVoteCountsProvidedByTheServer() {
        Comment comment = new Comment(1, "Test comment", "testuser");

        comment.setVoteCounts(2, 1);

        assertEquals(2, comment.getUpvotes());
        assertEquals(1, comment.getDownvotes());
    }

    @Test
    void shouldReplaceVoteCountsWhenTheyAreRefreshed() {
        Comment comment = new Comment(1, "Test comment", "testuser");
        comment.setVoteCounts(1, 0);

        assertEquals(1, comment.getUpvotes());
        assertEquals(0, comment.getDownvotes());

        comment.setVoteCounts(0, 1);

        assertEquals(0, comment.getUpvotes());
        assertEquals(1, comment.getDownvotes());
    }

    @Test
    void shouldUpdateCountsWhenAVoteIsRemoved() {
        Comment comment = new Comment(1, "Test comment", "testuser");
        comment.setVoteCounts(1, 1);

        comment.setVoteCounts(0, 1);

        assertEquals(0, comment.getUpvotes());
        assertEquals(1, comment.getDownvotes());
    }
}

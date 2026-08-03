package post.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    void shouldCreatePostWithInitialDefaults() {
        Post post = new Post(1, "Test Title", "Test content", "testuser", "testsubreddit");

        assertEquals(1, post.getId());
        assertEquals("Test Title", post.getTitle());
        assertEquals("Test content", post.getContent());
        assertEquals("testuser", post.getAuthor());
        assertEquals("testsubreddit", post.getSubredditName());
        assertNotNull(post.getComments());
        assertTrue(post.getComments().isEmpty());
        assertEquals(0, post.getUpvotes());
        assertEquals(0, post.getDownvotes());
    }

    @Test
    void shouldAddAndMaintainMultipleComments() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");
        Comment comment1 = new Comment(1, "Comment 1", "user1");
        Comment comment2 = new Comment(2, "Comment 2", "user2");

        post.addComment(comment1);
        post.addComment(comment2);

        List<Comment> comments = post.getComments();
        assertEquals(2, comments.size());
        assertEquals("Comment 1", comments.get(0).getText());
        assertEquals("Comment 2", comments.get(1).getText());
    }

    @Test
    void shouldRemoveCommentByValidIndex() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");
        Comment comment1 = new Comment(1, "Comment 1", "user1");
        Comment comment2 = new Comment(2, "Comment 2", "user2");
        Comment comment3 = new Comment(3, "Comment 3", "user3");

        post.addComment(comment1);
        post.addComment(comment2);
        post.addComment(comment3);

        post.getComments().remove(1);

        List<Comment> comments = post.getComments();
        assertEquals(2, comments.size());
        assertEquals("Comment 1", comments.get(0).getText());
        assertEquals("Comment 3", comments.get(1).getText());
    }

    @Test
    void shouldHandleInvalidIndexWhenRemovingComment() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");
        Comment comment = new Comment(1, "Comment", "user");
        post.addComment(comment);

        assertThrows(IndexOutOfBoundsException.class, () -> post.getComments().remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> post.getComments().remove(5));
        assertEquals(1, post.getComments().size());

        Post emptyPost = new Post(2, "Title", "Content", "user", "subreddit");
        assertThrows(IndexOutOfBoundsException.class, () -> emptyPost.getComments().remove(0));
        assertEquals(0, emptyPost.getComments().size());
    }

    @Test
    void shouldUpdateVoteCounts() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");

        post.setVoteCounts(1, 1);

        assertEquals(1, post.getUpvotes());
        assertEquals(1, post.getDownvotes());
    }

    @Test
    void shouldExposeVoteCountsProvidedByTheServer() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");

        post.setVoteCounts(2, 1);

        assertEquals(2, post.getUpvotes());
        assertEquals(1, post.getDownvotes());
    }

    @Test
    void shouldReplaceVoteCountsWhenTheyAreRefreshed() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");
        post.setVoteCounts(1, 0);

        assertEquals(1, post.getUpvotes());
        assertEquals(0, post.getDownvotes());

        post.setVoteCounts(0, 1);

        assertEquals(0, post.getUpvotes());
        assertEquals(1, post.getDownvotes());
    }

    @Test
    void shouldUpdateCountsWhenAVoteIsRemoved() {
        Post post = new Post(1, "Title", "Content", "user", "subreddit");
        post.setVoteCounts(1, 1);

        post.setVoteCounts(0, 1);

        assertEquals(0, post.getUpvotes());
        assertEquals(1, post.getDownvotes());
    }
}

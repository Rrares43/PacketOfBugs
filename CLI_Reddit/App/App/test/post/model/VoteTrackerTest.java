package post.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoteTrackerTest {

    @Test
    void shouldInitializeWithZeroVotes() {
        VoteTracker voteTracker = new VoteTracker();

        assertEquals(0, voteTracker.getUpvotes());
        assertEquals(0, voteTracker.getDownvotes());
    }

    @Test
    void shouldIncrementUpvotesAndDownvotes() {
        VoteTracker voteTracker = new VoteTracker();

        voteTracker.addUpvotes();
        voteTracker.addUpvotes();
        voteTracker.addDownvotes();

        assertEquals(2, voteTracker.getUpvotes());
        assertEquals(1, voteTracker.getDownvotes());
    }

    @Test
    void shouldDecrementUpvotesAndDownvotes() {
        VoteTracker voteTracker = new VoteTracker();

        voteTracker.addUpvotes();
        voteTracker.addUpvotes();
        voteTracker.addDownvotes();

        voteTracker.removeUpvotes();
        voteTracker.removeDownvotes();

        assertEquals(1, voteTracker.getUpvotes());
        assertEquals(0, voteTracker.getDownvotes());
    }

    @Test
    void shouldHandleNegativeVotesWhenRemovingFromZero() {
        VoteTracker voteTracker = new VoteTracker();

        voteTracker.removeUpvotes();
        voteTracker.removeDownvotes();

        assertEquals(-1, voteTracker.getUpvotes());
        assertEquals(-1, voteTracker.getDownvotes());
    }
}

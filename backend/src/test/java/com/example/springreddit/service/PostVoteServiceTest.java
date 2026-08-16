package com.example.springreddit.service;

import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Contains a unit test for the voteOnPost() method in PostVoteService.
 */
@ExtendWith(MockitoExtension.class)
public class PostVoteServiceTest {
    @InjectMocks
    private PostVoteService postVoteService;
    @Mock
    private PostVoteRepository postVoteRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AccountRepository accountRepository;

    /**
     * Leave a vote on an existing post, existing vote is an upvote.
     * Expected result: returns a VoteResponse, where new vote is "down" with no errors.
     */
    @Test
    public void testVoteOnPost_ExistingVoteIsUp() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setAuthor(mockAccount);

        when(accountRepository.findIdByUsername("test_user")).thenReturn(Optional.of(1L));
        when(postVoteRepository.findVoteTypeByPostIdAndAccountId(mockPost.getId(), 1L)).thenReturn(Optional.of((short) 1));

        VoteResponse response = postVoteService.voteOnPost(mockPost.getId(), "test_user", "down");

        assertNotNull(response);
        assertEquals("down", response.userVote());
    }
}

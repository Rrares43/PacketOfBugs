package com.example.springreddit.controller;

import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.model.*;
import com.example.springreddit.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of integration tests for each method in CommentController.
 */
@AutoConfigureMockMvc
public class CommentControllerIT extends BaseIntegrationTest{
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private SubredditRepository subredditRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a valid comment.
     * Expected result: comment persists with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testCreateComment() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        Subreddit subreddit = new Subreddit();
        subreddit.setName("test_sub");
        subreddit.setDisplayName("test sub");
        subreddit.setDescription("for testing");
        subreddit.setCreator(account);
        subredditRepository.save(subreddit);

        Post post = new Post();
        post.setTitle("test_post");
        post.setContent("test_content");
        post.setSubreddit(subreddit);
        post.setAuthor(account);
        postRepository.save(post);
        UUID postId = postRepository.findAllWithAuthorAndSubreddit().get(0).getId();

        CreateCommentRequest request = new CreateCommentRequest(
                "test comment",
                null
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/posts/{id}/comments", postId)
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("test comment"));

        assertEquals("test comment", commentRepository.findAllByPostIdWithDetails(postId).get(0).getContent());
    }

    /**
     * Update an existing comment.
     * Expected result: comment updated with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testUpdateComment() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        Subreddit subreddit = new Subreddit();
        subreddit.setName("test_sub");
        subreddit.setDisplayName("test sub");
        subreddit.setDescription("for testing");
        subreddit.setCreator(account);
        subredditRepository.save(subreddit);

        Post post = new Post();
        post.setTitle("test_post");
        post.setContent("test_content");
        post.setSubreddit(subreddit);
        post.setAuthor(account);
        postRepository.save(post);
        UUID postId = postRepository.findAllWithAuthorAndSubreddit().get(0).getId();

        Comment comment = new Comment();
        comment.setContent("test comment");
        comment.setPost(post);
        comment.setAuthor(account);
        comment.setParentComment(null);
        commentRepository.save(comment);
        UUID commentId = commentRepository.findAllByPostIdWithDetails(postId).get(0).getId();

        UpdateCommentRequest request = new UpdateCommentRequest("new content");

        mockMvc.perform(MockMvcRequestBuilders.put("/comments/{id}", commentId)
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("new content"));

        assertEquals("new content", commentRepository.findAllByPostIdWithDetails(postId).get(0).getContent());
    }

    /**
     * Delete an existing comment.
     * Expected result: comment deleted with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testDeleteComment() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        Subreddit subreddit = new Subreddit();
        subreddit.setName("test_sub");
        subreddit.setDisplayName("test sub");
        subreddit.setDescription("for testing");
        subreddit.setCreator(account);
        subredditRepository.save(subreddit);

        Post post = new Post();
        post.setTitle("test_post");
        post.setContent("test_content");
        post.setSubreddit(subreddit);
        post.setAuthor(account);
        postRepository.save(post);
        UUID postId = postRepository.findAllWithAuthorAndSubreddit().get(0).getId();

        Comment comment = new Comment();
        comment.setContent("test comment");
        comment.setPost(post);
        comment.setAuthor(account);
        comment.setParentComment(null);
        commentRepository.save(comment);
        UUID commentId = commentRepository.findAllByPostIdWithDetails(postId).get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/comments/{id}", commentId)
                        .with(user(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("The comment was deleted successfully"));

        assertTrue(commentRepository.findAllByPostIdWithDetails(postId).get(0).isDeleted());
    }

    /**
     * Get comment by ID.
     * Expected result: returns the CommentResponse with the corresponding ID with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetComment() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        accountRepository.save(account);

        Subreddit subreddit = new Subreddit();
        subreddit.setName("test_sub");
        subreddit.setDisplayName("test sub");
        subreddit.setDescription("for testing");
        subreddit.setCreator(account);
        subredditRepository.save(subreddit);

        Post post = new Post();
        post.setTitle("test_post");
        post.setContent("test_content");
        post.setSubreddit(subreddit);
        post.setAuthor(account);
        postRepository.save(post);
        UUID postId = postRepository.findAllWithAuthorAndSubreddit().get(0).getId();

        Comment comment = new Comment();
        comment.setContent("test comment");
        comment.setPost(post);
        comment.setAuthor(account);
        comment.setParentComment(null);
        commentRepository.save(comment);
        UUID commentId = commentRepository.findAllByPostIdWithDetails(postId).get(0).getId();

        mockMvc.perform(get("/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("test comment"));
    }

    /**
     * Get comments by a valid post ID.
     * Expected result: returns a list of CommentResponse objects from the corresponding post with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetCommentsByPostId() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        accountRepository.save(account);

        Subreddit subreddit = new Subreddit();
        subreddit.setName("test_sub");
        subreddit.setDisplayName("test sub");
        subreddit.setDescription("for testing");
        subreddit.setCreator(account);
        subredditRepository.save(subreddit);

        Post post = new Post();
        post.setTitle("test_post");
        post.setContent("test_content");
        post.setSubreddit(subreddit);
        post.setAuthor(account);
        postRepository.save(post);
        UUID postId = postRepository.findAllWithAuthorAndSubreddit().get(0).getId();

        Comment comment = new Comment();
        comment.setContent("test comment");
        comment.setPost(post);
        comment.setAuthor(account);
        comment.setParentComment(null);
        commentRepository.save(comment);

        mockMvc.perform(get("/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("test comment"));
    }

    /**
     * Leave a valid vote on a comment.
     * Expected result: returns a valid VoteResponse with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testVote() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        Subreddit subreddit = new Subreddit();
        subreddit.setName("test_sub");
        subreddit.setDisplayName("test sub");
        subreddit.setDescription("for testing");
        subreddit.setCreator(account);
        subredditRepository.save(subreddit);

        Post post = new Post();
        post.setTitle("test_post");
        post.setContent("test_content");
        post.setSubreddit(subreddit);
        post.setAuthor(account);
        postRepository.save(post);
        UUID postId = postRepository.findAllWithAuthorAndSubreddit().get(0).getId();

        Comment comment = new Comment();
        comment.setContent("test comment");
        comment.setPost(post);
        comment.setAuthor(account);
        comment.setParentComment(null);
        commentRepository.save(comment);
        UUID commentId = commentRepository.findAllByPostIdWithDetails(postId).get(0).getId();

        VoteRequest request = new VoteRequest("up");

        mockMvc.perform(MockMvcRequestBuilders.put("/comments/{id}/vote", commentId)
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userVote").value("up"));
    }
}
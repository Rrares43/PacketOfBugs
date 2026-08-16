package com.example.springreddit.controller;

import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.SubredditRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of integration tests for each method in PostController.
 */
@AutoConfigureMockMvc
public class PostControllerIT extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private SubredditRepository subredditRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a valid post.
     * Expected result: post persists with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testCreatePost() throws Exception {
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

        mockMvc.perform(multipart("/posts")
                        .with(user(details))
                        .param("title", "test post")
                        .param("content", "test content")
                        .param("subreddit", "test_sub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("test post"));

        assertEquals("test post", postRepository.findAllWithAuthorAndSubreddit().get(0).getTitle());
    }

    /**
     * Update an existing post.
     * Expected result: post updated with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testUpdatePost() throws Exception {
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

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("new_title");
        request.setContent("new_content");

        mockMvc.perform(MockMvcRequestBuilders.put("/posts/{id}", postId)
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("new_title"))
                .andExpect(jsonPath("$.data.content").value("new_content"));

        assertEquals("new_title", postRepository.findAllWithAuthorAndSubreddit().get(0).getTitle());
    }

    /**
     * Delete an existing post.
     * Expected result: post deleted with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testDeletePost() throws Exception {
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

        mockMvc.perform(MockMvcRequestBuilders.delete("/posts/{id}", postId)
                        .with(user(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Postarea a fost stearsa cu succes"));

        assertTrue(postRepository.findAllWithAuthorAndSubreddit().get(0).isDeleted());
    }

    /**
     * Get all posts.
     * Expected result: returns a list of PostResponse objects for all posts with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetPosts() throws Exception {
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

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("test_post"));
    }

    /**
     * Get posts by subreddit name.
     * Expected result: returns a list of PostResponse objects from the corresponding subreddit with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetSubredditPosts() throws Exception {
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

        mockMvc.perform(get("/subreddits/{name}/posts", "test_sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("test_post"));
    }

    /**
     * Get post by ID.
     * Expected result: returns the PostResponse with the corresponding ID with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetPostById() throws Exception {
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

        mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("test_post"));
    }

    /**
     * Leave a valid vote on a post.
     * Expected result: returns a valid VoteResponse with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testVoteOnPost() throws Exception {
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

        VoteRequest request = new VoteRequest("up");

        mockMvc.perform(MockMvcRequestBuilders.put("/posts/{id}/vote", postId)
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userVote").value("up"));
    }
}

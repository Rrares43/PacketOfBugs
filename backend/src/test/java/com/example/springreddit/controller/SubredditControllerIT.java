package com.example.springreddit.controller;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.SubredditRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of integration tests for each method in SubredditController.
 */
@AutoConfigureMockMvc
public class SubredditControllerIT extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SubredditRepository subredditRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a valid subreddit.
     * Expected result: subreddit persists with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testCreateNewSubreddit() throws Exception {
        SubredditDto.CreateSubredditRequest request = new SubredditDto.CreateSubredditRequest(
                "test_sub",
                "test sub",
                "for testing",
                null
        );

        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");

        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        mockMvc.perform(MockMvcRequestBuilders.post("/subreddits")
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test_sub"));

        assertTrue(subredditRepository.existsByName("test_sub"));
    }

    /**
     * Edit an existing subreddit.
     * Expected result: subreddit edited with no errors
     * @throws Exception
     */
    @Test
    @Transactional
    public void testEditSubreddit() throws Exception {
        SubredditDto.EditSubredditRequest request = new SubredditDto.EditSubredditRequest(
                "new name",
                "new description",
                "iconurl.com"
        );

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

        mockMvc.perform(MockMvcRequestBuilders.put("/subreddits/{name}", "test_sub")
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("new name"))
                .andExpect(jsonPath("$.data.description").value("new description"))
                .andExpect(jsonPath("$.data.iconUrl").value("iconurl.com"));

        Subreddit editedSubreddit = subredditRepository.findByName("test_sub")
                .orElseThrow(() -> new Exception());

        assertEquals("new name", editedSubreddit.getDisplayName());
    }

    /**
     * Delete an existing subreddit with no posts.
     * Expected result: subreddit deleted with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testDeleteSubreddit() throws Exception{
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

        mockMvc.perform(MockMvcRequestBuilders.delete("/subreddits/{name}", "test_sub")
                        .with(user(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Subreddit deleted successfully"));

        assertFalse(subredditRepository.existsByName("test_sub"));
    }

    /**
     * Get all subreddits.
     * Expected result: returns a list of SubredditResponse objects for all subreddits with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetAllSubreddits() throws Exception {
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

        mockMvc.perform(get("/subreddits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("test_sub"));
    }

    /**
     * Get subreddits by the creator's username.
     * Expected result: returns a list of SubredditResponse objects with the corresponding creator username.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetByCreator() throws Exception {
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

        mockMvc.perform(get("/subreddits/by-creator/{username}", "test_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("test_sub"));
    }

    /**
     * Get subreddit by name.
     * Expected result: returns the SubredditResponse with the corresponding name.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetSubredditByName() throws Exception{
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

        mockMvc.perform(get("/subreddits/{name}", "test_sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test_sub"));
    }
}

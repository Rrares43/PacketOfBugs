package com.example.springreddit.service;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.SubredditRepository;
import com.example.springreddit.repository.SubredditSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Contains a set of unit tests for each method in SubredditService.
 */
@ExtendWith(MockitoExtension.class)
public class SubredditServiceTest {
    @InjectMocks
    private SubredditService subredditService;
    @Mock
    private SubredditRepository subredditRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PostService postService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Create a valid subreddit.
     * Expected result: returns mockSubreddit with no errors.
     */
    @Test
    public void testCreateSubreddit() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        SubredditDto.CreateSubredditRequest mockRequest = new SubredditDto.CreateSubredditRequest(
                "test_sub",
                "Test Sub",
                "Sub for testing",
                "iconurl.com"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(subredditRepository.existsByName("test_sub")).thenReturn(false);
        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(subredditRepository.save(any(Subreddit.class))).thenReturn(mockSubreddit);

        Subreddit createdSubreddit = subredditService.createSubreddit(mockRequest);
        assertEquals(mockSubreddit, createdSubreddit);
    }

    /**
     * Get subreddits by the creator's username.
     * Expected result: returns a list that contains mockSubreddit with no errors.
     */
    @Test
    public void testGetSubredditsByCreatorUsername() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        List<Subreddit> mockList = new ArrayList<>();
        mockList.add(mockSubreddit);

        when(subredditRepository.findByCreator_Username("test_user")).thenReturn(mockList);

        List<Subreddit> retrievedList = subredditService.getSubredditsByCreatorUsername("test_user");
        assertEquals(mockList, retrievedList);
    }

    /**
     * Get an existing subreddit by name
     * Expected result: returns mockSubreddit with no errors.
     */
    @Test
    public void testGetSubredditByName() {
        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");

        when(subredditRepository.findByName("test_sub")).thenReturn(Optional.of(mockSubreddit));

        Subreddit retrievedSubreddit = subredditService.getSubredditByName("test_sub");
        assertEquals(mockSubreddit, retrievedSubreddit);
    }

    /**
     * Edit an existing subreddit.
     * Expected result: mockSubreddit updated with no errors.
     */
    @Test
    public void testEditSubreddit() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        SubredditDto.EditSubredditRequest mockRequest = new SubredditDto.EditSubredditRequest(
                "New Display Name",
                "This is a new description",
                "newiconurl.com"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(subredditRepository.findByName("test_sub")).thenReturn(Optional.of(mockSubreddit));
        when(subredditRepository.save(any(Subreddit.class))).thenReturn(mockSubreddit);

        Subreddit editedSubreddit = subredditService.editSubreddit("test_sub", mockRequest);
        assertEquals(mockSubreddit, editedSubreddit);
    }

    /**
     * Delete an existing subreddit.
     * Expected result: mockSubreddit deleted with no errors.
     */
    @Test
    public void testDeleteSubreddit() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        AtomicReference<Subreddit> mockSubredditReference = new AtomicReference<>(mockSubreddit);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(subredditRepository.findByName("test_sub")).thenReturn(Optional.of(mockSubreddit));

        doAnswer(invocation -> {
            mockSubredditReference.set(null);
            return null;
        }).when(subredditRepository).deleteByName("test_sub");

        subredditService.deleteSubreddit("test_sub");

        assertTrue(mockSubredditReference.get() == null);
    }

    /**
     * Get all subreddit summaries.
     * Expected result: returns a list that contains mockSummary with no errors.
     */
    @Test
    public void testGetAllSubredditSummaries() {
        SubredditSummary mockSummary = mock(SubredditSummary.class);
        List<SubredditSummary> mockSummaryList = new ArrayList<>();
        mockSummaryList.add(mockSummary);

        when(subredditRepository.findAllSummaries()).thenReturn(mockSummaryList);

        List<SubredditSummary> retrievedList = subredditService.getAllSubredditSummaries();

        assertEquals(mockSummaryList, retrievedList);
    }
}
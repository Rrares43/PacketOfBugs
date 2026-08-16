package com.example.springreddit.service;

import com.example.springreddit.dto.*;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.VoteRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Contains a set of unit tests for each method in CommentService.
 */
@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @InjectMocks
    private CommentServiceImpl commentService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private FastContentFilterService contentFilterService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Get an existing comment by ID.
     * Expected result: returns the CommentResponse with fields corresponding to mockComment
     */
    @Test
    public void testGetComment() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        Comment mockComment = new Comment();
        mockComment.setId(UUID.randomUUID());
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(null);
        mockComment.setAuthor(mockAccount);

        when(commentRepository.findByIdWithReplies(mockComment.getId())).thenReturn(Optional.of(mockComment));

        CommentResponse response = commentService.getComment(mockComment.getId());

        assertNotNull(response);
        assertEquals(mockComment.getId(), response.id());
        assertEquals("Test comment", response.content());
        assertEquals(mockPost.getId(), response.postId());
        assertNull(response.parentId());
        assertEquals("test_user", response.author());
    }

    /**
     * Create a valid comment without a parent comment.
     * Expected result: returns the CommentResponse with fields corresponding to mockComment with no errors.
     */
    @Test
    public void testCreateComment_parentIsNull() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        Comment mockComment = new Comment();
        mockComment.setId(UUID.randomUUID());
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(null);
        mockComment.setAuthor(mockAccount);

        CreateCommentRequest mockRequest = new CreateCommentRequest(
                "Test comment",
                null
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        when(contentFilterService.sanitize(anyString())).thenReturn("Test comment");

        CommentResponse createdComment = commentService.createComment(mockPost.getId(), mockRequest);

        assertNotNull(createdComment);
        assertEquals(mockComment.getId(), createdComment.id());
        assertEquals("Test comment", createdComment.content());
        assertEquals(mockPost.getId(), createdComment.postId());
        assertNull(createdComment.parentId());
        assertEquals("test_user", createdComment.author());
    }

    /**
     * Create a valid comment with a parent comment.
     * Expected result: returns the CommentResponse with fields corresponding to mockComment with no errors.
     */
    @Test
    public void testCreateComment_parentIsNotNull() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        Comment mockParent = new Comment();
        mockParent.setId(UUID.randomUUID());
        mockParent.setContent("Test parent comment");
        mockParent.setPost(mockPost);
        mockParent.setParentComment(null);
        mockParent.setAuthor(mockAccount);

        Comment mockComment = new Comment();
        mockComment.setId(UUID.randomUUID());
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(mockParent);
        mockComment.setAuthor(mockAccount);

        CreateCommentRequest mockRequest = new CreateCommentRequest(
                "Test comment",
                null
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        when(contentFilterService.sanitize(anyString())).thenReturn("Test comment");

        CommentResponse createdComment = commentService.createComment(mockPost.getId(), mockRequest);

        assertNotNull(createdComment);
        assertEquals(mockComment.getId(), createdComment.id());
        assertEquals("Test comment", createdComment.content());
        assertEquals(mockPost.getId(), createdComment.postId());
        assertEquals(mockParent.getId(), createdComment.parentId());
        assertEquals("test_user", createdComment.author());
    }

    /**
     * Update an existing comment.
     * Expected result: returns the CommentResponse with fields corresponding to mockRequest with no errors.
     */
    @Test
    public void testUpdateComment() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        Comment mockComment = new Comment();
        mockComment.setId(UUID.randomUUID());
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(null);
        mockComment.setAuthor(mockAccount);

        UpdateCommentRequest mockRequest = new UpdateCommentRequest(
                "New content"
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(commentRepository.findById(mockComment.getId())).thenReturn(Optional.of(mockComment));
        when(contentFilterService.sanitize(anyString())).thenReturn("New content");
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        CommentResponse updatedComment = commentService.updateComment(mockComment.getId(), mockRequest);

        assertNotNull(updatedComment);
        assertEquals(mockComment.getId(), updatedComment.id());
        assertEquals("New content", updatedComment.content());
        assertEquals("test_user", updatedComment.author());
    }

    /**
     * Update an existing comment.
     * Expected result: comment deleted with no errors.
     */
    @Test
    public void testDeleteComment() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        Comment mockComment = new Comment();
        mockComment.setId(UUID.randomUUID());
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(null);
        mockComment.setAuthor(mockAccount);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(commentRepository.findById(mockComment.getId())).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        commentService.deleteComment(mockComment.getId());

        assertTrue(mockComment.isDeleted());
    }

    /**
     * Leave a vote on an existing comment.
     * Expected result: returns the VoteResponse with fields corresponding to mockVote with no errors.
     */
    @Test
    public void testVote() {
        SecurityContextHolder.setContext(securityContext);

        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        Comment mockComment = new Comment();
        mockComment.setId(UUID.randomUUID());
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(null);
        mockComment.setAuthor(mockAccount);

        VoteRequest mockRequest = new VoteRequest(
                "up"
        );

        CommentVote mockVote = new CommentVote();
        mockVote.setAccount(mockAccount);
        mockVote.setComment(mockComment);
        mockVote.setVoteType((short) 1);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(commentRepository.findById(mockComment.getId())).thenReturn(Optional.of(mockComment));
        when(voteRepository.findByComment_IdAndAccount_Id(mockComment.getId(), mockAccount.getId())).thenReturn(Optional.of(mockVote));

        VoteResponse response = commentService.vote(mockComment.getId(), mockRequest);

        assertNotNull(response);
        assertEquals("up", response.userVote());
    }

    /**
     * Get comments by post ID.
     * Expected result: returns a list that contains the CommentResponse object with fields corresponding to mockComment with no errors.
     */
    @Test
    public void testGetCommentsByPostId() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        UUID postId = UUID.randomUUID();

        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("Test post");
        mockPost.setAuthor(mockAccount);

        UUID commentId = UUID.randomUUID();

        Comment mockComment = new Comment();
        mockComment.setId(commentId);
        mockComment.setContent("Test comment");
        mockComment.setPost(mockPost);
        mockComment.setParentComment(null);
        mockComment.setAuthor(mockAccount);

        List<Comment> mockList = new ArrayList<>();
        mockList.add(mockComment);

        when(postRepository.existsById(postId)).thenReturn(true);
        when(commentRepository.findAllByPostIdWithDetails(postId)).thenReturn(mockList);

        List<CommentResponse> responseList = commentService.getCommentsByPostId(postId);
        CommentResponse response = responseList.get(0);

        assertEquals("Test comment", response.content());
    }
}

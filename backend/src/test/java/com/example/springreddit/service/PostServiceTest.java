package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.ImageStatus;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Contains a set of unit tests for each method in PostService.
 */
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @InjectMocks
    private PostService postService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private SubredditRepository subredditRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PostVoteRepository postVoteRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private FastContentFilterService contentFilterService;
    @Mock
    private ImageUploadService imageUploadService;
    @Mock
    private ImageOptimizationService imageOptimizationService;
    @Mock
    private ImageEditService imageEditService;
    @Mock
    private Executor imageThreadPool;

    @BeforeEach
    void runImageCompressionInline() {
        lenient().doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(imageThreadPool).execute(any(Runnable.class));
    }

    /**
     * Create a valid post.
     * Expected result: returns mockPost with no errors.
     */
    @Test
    public void testCreatePost() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        Post mockPost = new Post();
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setAuthor(mockAccount);
        mockPost.setSubreddit(mockSubreddit);

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(subredditRepository.findByName("test_sub")).thenReturn(Optional.of(mockSubreddit));
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);

        Post createdPost = postService.createPost(
                "Test post",
                "This post is for testing",
                "test_user",
                "test_sub",
                null,
                null
        );

        assertEquals(mockPost, createdPost);
        verifyNoInteractions(imageUploadService);
    }

    /**
     * Create a post with an image.
     * Expected result: compression and S3 upload complete before the post is persisted,
     * so the returned entity already contains the public image URL.
     */
    @Test
    public void testCreatePost_ReturnsPopulatedImageUrl() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        byte[] imageBytes = new byte[]{1, 2, 3, 4};
        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", imageBytes);
        OptimizedImageResult optimizedImage = new OptimizedImageResult(imageBytes, imageBytes.length, "image/jpeg");
        String imageUrl = "https://bucket.s3.eu-central-1.amazonaws.com/images/photo.jpg";

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(subredditRepository.findByName("test_sub")).thenReturn(Optional.of(mockSubreddit));
        when(imageOptimizationService.downscaleForFiltering(image)).thenReturn(imageBytes);
        when(imageEditService.applyFilter(imageBytes, 2)).thenReturn(imageBytes);
        when(imageOptimizationService.optimize(imageBytes, "image/jpeg", imageBytes.length)).thenReturn(optimizedImage);
        when(imageUploadService.upload(any(), eq((long) imageBytes.length), eq("image/jpeg"), eq(".jpg"), isNull()))
                .thenReturn(imageUrl);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post createdPost = postService.createPost(
                "Test post",
                "This post is for testing",
                "test_user",
                "test_sub",
                image,
                2
        );

        assertEquals(imageUrl, createdPost.getImageUrl());
        assertEquals(ImageStatus.COMPLETED, createdPost.getImageStatus());
        verify(imageOptimizationService).validateUpload(image);
        verify(imageOptimizationService).downscaleForFiltering(image);
        verify(imageEditService).applyFilter(imageBytes, 2);
        verify(imageOptimizationService).optimize(imageBytes, "image/jpeg", imageBytes.length);
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(savedPost.capture());
        assertEquals(imageUrl, savedPost.getValue().getImageUrl());
        assertEquals(ImageStatus.COMPLETED, savedPost.getValue().getImageStatus());
    }

    /**
     * Create a post with an image when S3 upload fails.
     * Expected result: a descriptive error is thrown and the post is not persisted.
     */
    @Test
    public void testCreatePost_ImageUploadFailureDoesNotPersistPost() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");

        byte[] imageBytes = new byte[]{1, 2, 3, 4};
        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.jpg", "image/jpeg", imageBytes);
        OptimizedImageResult optimizedImage = new OptimizedImageResult(imageBytes, imageBytes.length, "image/jpeg");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(subredditRepository.findByName("test_sub")).thenReturn(Optional.of(mockSubreddit));
        when(imageOptimizationService.downscaleForFiltering(image)).thenReturn(imageBytes);
        when(imageOptimizationService.optimize(imageBytes, "image/jpeg", imageBytes.length)).thenReturn(optimizedImage);
        when(imageUploadService.upload(any(), eq((long) imageBytes.length), eq("image/jpeg"), eq(".jpg"), isNull()))
                .thenThrow(new RuntimeException("S3 unavailable"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                postService.createPost(
                        "Test post",
                        "This post is for testing",
                        "test_user",
                        "test_sub",
                        image,
                        null
                ));

        assertEquals("Image upload failed. Please try again.", exception.getMessage());
        verifyNoInteractions(postRepository);
    }

    /**
     * Get post by ID.
     * Expected result: returns mockPost with no errors.
     */
    @Test
    public void testGetPostById() {
        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");

        when(postRepository.findByIdWithAuthorAndSubreddit(mockPost.getId())).thenReturn(Optional.of(mockPost));

        Post retrievedPost = postService.getPostById(mockPost.getId());

        assertEquals(mockPost, retrievedPost);
    }

    /**
     * Get all posts.
     * Expected result: returns a list of Post objects that contains mockPost with no errors.
     */
    @Test
    public void testGetAllPosts() {
        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");

        List<Post> mockList = new ArrayList<>();
        mockList.add(mockPost);

        when(postRepository.findAllWithAuthorAndSubreddit()).thenReturn(mockList);

        List<Post> retrievedList = postService.getAllPosts();

        assertEquals(mockList, retrievedList);
    }

    /**
     * Get posts by subreddit name.
     * Expected result: returns a list that contains mockPost with no errors.
     */
    @Test
    public void testGetPostsBySubreddit() {
        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");

        Post mockPost = new Post();
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setSubreddit(mockSubreddit);

        List<Post> mockList = new ArrayList<>();
        mockList.add(mockPost);

        when(postRepository.findBySubredditNameWithAuthorAndSubreddit("test_sub")).thenReturn(mockList);

        List<Post> retrievedList = postService.getPostsBySubreddit("test_sub");

        assertEquals(mockList, retrievedList);
    }

    /**
     * Get all posts or by subreddit name.
     * Expected result (subreddit name is given as an argument): returns a list that contains mockPost with no errors.
     */
    @Test
    public void testGetAllPostsOrBySubreddit_SubredditNameIsNotNull() {
        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");

        Post mockPost = new Post();
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setSubreddit(mockSubreddit);

        Post mockPost2 = new Post();
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");

        List<Post> mockList = new ArrayList<>();
        mockList.add(mockPost);

        when(postRepository.findBySubredditNameWithAuthorAndSubreddit("test_sub")).thenReturn(mockList);

        List<Post> retrievedListBySubreddit = postService.getAllPostsOrBySubreddit("test_sub");

        assertEquals(mockList, retrievedListBySubreddit);
    }

    /**
     * Get all posts or by subreddit name.
     * Expected result (subreddit name is null): returns a list that contains mockPost and mockPost2 with no errors.
     */
    @Test
    public void testGetAllPostsOrBySubreddit_SubredditNameIsNull() {
        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");

        Post mockPost = new Post();
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setSubreddit(mockSubreddit);

        Post mockPost2 = new Post();
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");


        List<Post> mockListAll = new ArrayList<>();
        mockListAll.add(mockPost);
        mockListAll.add(mockPost2);

        when(postRepository.findAllWithAuthorAndSubreddit()).thenReturn(mockListAll);

        List<Post> retrievedList = postService.getAllPostsOrBySubreddit(null);

        assertEquals(mockListAll, retrievedList);
    }

    /**
     * Update an existing post.
     * Expected result: mockPost updated with no errors.
     */
    @Test
    public void testUpdatePost() {
        UpdatePostRequest mockRequest = new UpdatePostRequest();
        mockRequest.setTitle("New title");
        mockRequest.setContent("New content");

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("Test Sub");
        mockSubreddit.setDescription("Sub for testing");
        mockSubreddit.setCreator(mockAccount);

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setAuthor(mockAccount);
        mockPost.setSubreddit(mockSubreddit);

        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);
        when(contentFilterService.sanitize(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        Post editedPost = postService.updatePost(mockPost.getId(), mockRequest, "test_user");

        assertEquals("New title", editedPost.getTitle());
        assertEquals("New content", editedPost.getContent());
    }

    /**
     * Resolve user vote for an existing post
     * Expected result: returns the user vote as a string with no errors.
     */
    @Test
    public void testResolveUserVote() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setAuthor(mockAccount);

        when(postVoteRepository.findVoteTypeByPostIdAndAccountUsername(mockPost.getId(), "test_user"))
                .thenReturn(Optional.of(PostVote.UPVOTE));

        String userVote = postService.resolveUserVote(mockPost, "test_user");

        assertEquals("up", userVote);
    }

    /**
     * Delete an existing post.
     * Expected result: mockPost deleted with no errors.
     */
    @Test
    public void testDeletePost() {
        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");
        mockPost.setAuthor(mockAccount);

        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);

        postService.deletePost(mockPost.getId(), "test_user");

        assertTrue(mockPost.isDeleted());
    }

    /**
     * Convert a list of post to PostResponse objects.
     * Expected result: returns a list that contains the PostResponse with fields corresponding with mockPost.
     */
    @Test
    public void testToPostResponses() {
        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");

        List<Post> mockList = new ArrayList<>();
        mockList.add(mockPost);

        List<UUID> mockIdList = new ArrayList<>();
        mockIdList.add(postId);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setId(1L);

        when(accountRepository.findByUsername("test_user"))
                .thenReturn(Optional.of(mockAccount));

        Object[] upvoteRow = new Object[] { postId, (short) PostVote.UPVOTE, 3L };
        Object[] downvoteRow = new Object[] { postId, (short) PostVote.DOWNVOTE, 1L };
        Object[] commentRow = new Object[] { postId, 2L };
        List<Object[]> voteCounts = Arrays.asList(upvoteRow, downvoteRow);
        List<Object[]> commentCounts = Collections.singletonList(commentRow);

        when(postVoteRepository.countGroupedByPostIds(mockIdList)).thenReturn(voteCounts);
        when(commentRepository.countGroupedByPostIds(mockIdList)).thenReturn(commentCounts);

        Object[] userVoteRow = new Object[]{ postId, (short) PostVote.UPVOTE };
        List<Object[]> userVotes = Collections.singletonList(userVoteRow);

        when(postVoteRepository.findVoteTypesByPostIdsAndAccountId(mockIdList, 1L))
                .thenReturn(userVotes);

        List<PostDto.PostResponse> responseList = postService.toPostResponses(mockList, "test_user");
        PostDto.PostResponse response = responseList.get(0);

        assertNotNull(response);
        assertEquals("Test post", response.title());
        assertEquals("This post is for testing", response.content());
        assertEquals(3L, response.upvotes());
        assertEquals(1L, response.downvotes());
        assertEquals(2L, response.score());
        assertEquals(2L, response.commentCount());
    }

    /**
     * Convert a post to a PostResponse.
     * Expected result: returns the PostResponse with fields corresponding with mockPost.
     */
    @Test
    public void testToPostResponse() {
        Post mockPost = new Post();
        mockPost.setId(UUID.randomUUID());
        mockPost.setTitle("Test post");
        mockPost.setContent("This post is for testing");

        Object[] upvoteRow = new Object[] { (short) PostVote.UPVOTE, 3L };
        Object[] downvoteRow = new Object[] { (short) PostVote.DOWNVOTE, 1L };
        List<Object[]> voteCounts = Arrays.asList(upvoteRow, downvoteRow);

        when(postVoteRepository.countGroupedByPostId(mockPost.getId())).thenReturn(voteCounts);
        when(commentRepository.countByPost_Id(mockPost.getId())).thenReturn(2L);

        PostDto.PostResponse response = postService.toPostResponse(mockPost, "up");

        assertNotNull(response);
        assertEquals("Test post", response.title());
        assertEquals("This post is for testing", response.content());
        assertEquals(3L, response.upvotes());
        assertEquals(1L, response.downvotes());
        assertEquals(2L, response.score());
        assertEquals(2L, response.commentCount());
    }
}

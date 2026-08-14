package com.example.springreddit.service;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    }

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

    @Test
    public void testGetAllPostsOrBySubreddit() {
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

        List<Post> mockListAll = new ArrayList<>();
        mockListAll.add(mockPost);
        mockListAll.add(mockPost2);

        when(postRepository.findBySubredditNameWithAuthorAndSubreddit("test_sub")).thenReturn(mockList);
        when(postRepository.findAllWithAuthorAndSubreddit()).thenReturn(mockListAll);

        List<Post> retrievedListBySubreddit = postService.getAllPostsOrBySubreddit("test_sub");

        assertEquals(mockList, retrievedListBySubreddit);

        List<Post> retrievedList = postService.getAllPostsOrBySubreddit(null);

        assertEquals(mockListAll, retrievedList);
    }

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

        when(postVoteRepository.findVoteTypeByPostIdAndAccountUsername(mockPost.getId(), "test_user"))
                .thenReturn(Optional.of(PostVote.DOWNVOTE));

        userVote = postService.resolveUserVote(mockPost, "test_user");

        assertEquals("down", userVote);
    }

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

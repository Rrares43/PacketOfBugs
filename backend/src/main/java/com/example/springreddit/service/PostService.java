package com.example.springreddit.service;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.exception.ForbiddenException;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import com.example.springreddit.repository.SubredditRepository;
import com.example.springreddit.util.TextFormatterUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final AccountRepository accountRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentRepository commentRepository;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private final ImageUploadService imageUploadService;
    private final FastContentFilterService contentFilterService;


    public PostService(PostRepository postRepository,
                       SubredditRepository subredditRepository,
                       AccountRepository accountRepository,
                       PostVoteRepository postVoteRepository,
                       CommentRepository commentRepository,
                       ImageUploadService imageUploadService,
                       FastContentFilterService contentFilterService) {
        this.postRepository = postRepository;
        this.subredditRepository = subredditRepository;
        this.accountRepository = accountRepository;
        this.postVoteRepository = postVoteRepository;
        this.commentRepository = commentRepository;
        this.imageUploadService = imageUploadService;
        this.contentFilterService = contentFilterService;
    }

    @Transactional
    public Post createPost(String title, String content, String authorUsername, String subredditName,
                          MultipartFile image, Integer filter) {
        validatePostTitle(title);
        validateContent(content);

        String formattedTitle = contentFilterService.sanitize(TextFormatterUtil.formatText(title));
        String formattedContent = contentFilterService.sanitize(TextFormatterUtil.formatText(content));

        validateAuthor(authorUsername);
        validateSubreddit(subredditName);
        
        Account author = accountRepository.findByUsername(authorUsername)
                .orElseThrow(() -> {
                    LOGGER.warn("Create post failed: author not found with username: {}", authorUsername);
                    return new IllegalArgumentException("Author not found");
                });

        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> {
                    LOGGER.warn("Create post failed: subreddit not found: {}", subredditName);
                    return new IllegalArgumentException("Subreddit not found");
                });

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = imageUploadService.upload(image, filter);
        }

        Post post = new Post(formattedTitle, formattedContent, author, subreddit, imageUrl, filter);
        Post savedPost = postRepository.save(post);
        
        PostVote upvote = new PostVote(author, savedPost, PostVote.UPVOTE);
        postVoteRepository.save(upvote);
        LOGGER.info("Post created successfully with ID: {} in subreddit: {} by author: {}", savedPost.getId(), subredditName, authorUsername);
        return savedPost;
    }

    @Transactional(readOnly = true)
    public Post getPostById(UUID postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        return postRepository.findByIdWithAuthorAndSubreddit(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAllWithAuthorAndSubreddit();
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsBySubreddit(String subredditName) {
        if (subredditName == null || subredditName.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        return postRepository.findBySubredditNameWithAuthorAndSubreddit((subredditName));
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPostsOrBySubreddit(String subredditName) {
        return (subredditName != null && !subredditName.isBlank())
                ? this.getPostsBySubreddit(subredditName)
                : this.getAllPosts();
    }

    @Transactional
    public Post updatePost(UUID id, UpdatePostRequest request, String currentUsername) {
        if (id == null) {
            LOGGER.warn("Update post failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (currentUsername == null || currentUsername.isBlank()) {
            LOGGER.warn("Update post failed: missing authenticated username");
            throw new UnauthorizedException("Authentication is required");
        }
        if (request == null) {
            LOGGER.warn("Update post failed: request body is null");
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Update post failed: post not found with ID: {}", id);
                    return new ResourceNotFoundException("Post not found: " + id);
                });

        if (post.getAuthor() == null || !currentUsername.equals(post.getAuthor().getUsername())) {
            LOGGER.warn(
                    "Update post failed: user '{}' is not the author of post ID: {}", currentUsername, id);
            throw new ForbiddenException("Only the post author can update it");
        }

        if (request.getTitle() != null) {
            validatePostTitle(request.getTitle());
            post.setTitle(contentFilterService.sanitize(request.getTitle().trim()));
        }
        if (request.getContent() != null) {
            validateContent(request.getContent());

            String formattedContent = contentFilterService.sanitize(TextFormatterUtil.formatText(request.getContent()));
            String formattedTitle = contentFilterService.sanitize(TextFormatterUtil.formatText(request.getTitle()));

            post.setContent(formattedContent);
            post.setTitle(formattedTitle);
        }

        post.setUpdatedAt(Instant.now());
        Post savedPost = postRepository.save(post);
        LOGGER.info(
                "Post updated successfully with ID: {} by author: {}", id, currentUsername);
        return savedPost;
    }

    @Transactional(readOnly = true)
    public String resolveUserVote(Post post, String username) {
        if (post == null || username == null || username.isBlank()) {
            return null;
        }
        return postVoteRepository.findVoteTypeByPostIdAndAccountUsername(post.getId(), username)
                .map(voteType -> voteType == PostVote.UPVOTE ? "up" : "down")
                .orElse("none");
    }

    @Transactional
    public void deletePost(UUID id, String currentUsername) {
        if (id == null) {
            LOGGER.warn("Delete post failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (currentUsername == null || currentUsername.isBlank()) {
            LOGGER.warn("Delete post failed: missing authenticated username");
            throw new UnauthorizedException("Authentication is required");
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Delete post failed: post not found with ID: {}", id);
                    return new ResourceNotFoundException("Post not found: " + id);
                });

        if (post.getAuthor() == null || !currentUsername.equals(post.getAuthor().getUsername())) {
            LOGGER.warn(
                    "Delete post failed: user '{}' is not the author of post ID: {}", currentUsername, id);
            throw new ForbiddenException("Only the post author can delete it");
        }

        if(!post.isDeleted()){
            post.softDelete();
            postRepository.save(post);
        }
        LOGGER.info(
                "Post deleted successfully with ID: {} by author: {}", id, currentUsername);
    }

    private String normalizeSubredditName(String subredditName) {
        if (subredditName == null) {
            return null;
        }
        return subredditName.startsWith("r/") ? subredditName : "r/" + subredditName;
    }

    private void validatePostTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required and cannot be blank");
        }
        if (title.length() < 3 || title.length() > 300) {
            throw new IllegalArgumentException("Title must be between 3 and 300 characters");
        }
    }

    private void validateContent(String content) {
        if (content != null && content.length() > 10000) {
            throw new IllegalArgumentException("Content must not exceed 10000 characters");
        }
    }

    private void validateAuthor(String authorUsername) {
        if (authorUsername == null || authorUsername.isBlank()) {
            throw new IllegalArgumentException("Author is required and cannot be blank");
        }
    }

    private void validateSubreddit(String subredditName) {
        if (subredditName == null || subredditName.isBlank()) {
            throw new IllegalArgumentException("Subreddit is required and cannot be blank");
        }
    }

    @Transactional(readOnly = true)
    public List<PostDto.PostResponse> toPostResponses(List<Post> posts, String currentUsername) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        Map<UUID, long[]> voteCounts = loadPostVoteCounts(postIds);
        Map<UUID, Long> commentCounts = loadCommentCounts(postIds);
        Map<UUID, String> userVotes = loadUserVotes(postIds, currentUsername);

        return posts.stream()
                .map(post -> {
                    long[] counts = voteCounts.getOrDefault(post.getId(), new long[]{0L, 0L});
                    long upvotes = counts[0];
                    long downvotes = counts[1];
                    long commentCount = commentCounts.getOrDefault(post.getId(), 0L);
                    String userVote = userVotes.get(post.getId());
                    return buildPostResponse(post, upvotes, downvotes, commentCount, userVote);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PostDto.PostResponse toPostResponse(Post post, String userVote) {
        long upvotes = 0L;
        long downvotes = 0L;
        for (Object[] row : postVoteRepository.countGroupedByPostId(post.getId())) {
            short type = ((Number) row[0]).shortValue();
            long count = ((Number) row[1]).longValue();
            if (type == PostVote.UPVOTE) {
                upvotes = count;
            } else if (type == PostVote.DOWNVOTE) {
                downvotes = count;
            }
        }
        long commentCount = commentRepository.countByPost_Id(post.getId());
        return buildPostResponse(post, upvotes, downvotes, commentCount, userVote);
    }

    private PostDto.PostResponse buildPostResponse(
            Post post, long upvotes, long downvotes, long commentCount, String userVote) {

        String title = post.getTitle();
        String content = post.getContent();
        String imageUrl = post.getImageUrl();
        String authorName = "unknown";


        if (post.getAuthor() != null) {
            if (post.getAuthor().isDeleted()) {
                authorName = "[deleted]";
            } else {
                authorName = post.getAuthor().getUsername();
            }
        }

        if (post.isDeleted()) {
            title = "[deleted]";
            content = "[deleted]";
            imageUrl = null;
            authorName = "[deleted]";
        }

        String subredditName = (post.getSubreddit() != null) ? post.getSubreddit().getName() : "unknown";

        return new PostDto.PostResponse(
                post.getId(),
                title,
                content,
                imageUrl,
                post.getFilter(),
                authorName,
                subredditName,
                upvotes,
                downvotes,
                upvotes - downvotes,
                commentCount,
                userVote,
                post.getCreatedAt() != null ? post.getCreatedAt().toString() : null,
                post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null
        );
    }

    private Map<UUID, long[]> loadPostVoteCounts(Collection<UUID> postIds) {
        Map<UUID, long[]> counts = new HashMap<>();
        for (Object[] row : postVoteRepository.countGroupedByPostIds(postIds)) {
            UUID postId = (UUID) row[0];
            short voteType = ((Number) row[1]).shortValue();
            long count = ((Number) row[2]).longValue();
            long[] bucket = counts.computeIfAbsent(postId, id -> new long[]{0L, 0L});
            if (voteType == PostVote.UPVOTE) {
                bucket[0] = count;
            } else if (voteType == PostVote.DOWNVOTE) {
                bucket[1] = count;
            }
        }
        return counts;
    }

    private Map<UUID, Long> loadCommentCounts(Collection<UUID> postIds) {
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : commentRepository.countGroupedByPostIds(postIds)) {
            counts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    private Map<UUID, String> loadUserVotes(Collection<UUID> postIds, String currentUsername) {
        Map<UUID, String> userVotes = new HashMap<>();
        if (currentUsername == null || currentUsername.isBlank()) {
            return userVotes;
        }

        Optional<Account> account = accountRepository.findByUsername(currentUsername);
        if (account.isEmpty()) {
            return userVotes;
        }

        for (Object[] row : postVoteRepository.findVoteTypesByPostIdsAndAccountId(
                postIds, account.get().getId())) {
            UUID postId = (UUID) row[0];
            short voteType = ((Number) row[1]).shortValue();
            userVotes.put(postId, voteType == PostVote.UPVOTE ? "up" : "down");
        }

        for (UUID postId : postIds) {
            userVotes.putIfAbsent(postId, "none");
        }
        return userVotes;
    }
}

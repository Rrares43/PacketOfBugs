package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.exception.ForbiddenException;
import com.example.springreddit.exception.ImageSizeExceededException;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.ImageStatus;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import com.example.springreddit.repository.SubredditRepository;
import com.example.springreddit.util.TextFormatterUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final AccountRepository accountRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentRepository commentRepository;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private final ImageUploadService imageUploadService;
    private final ImageOptimizationService imageOptimizationService;
    private final ImageEditService imageEditService;
    private final Executor imageThreadPool;
    private final FastContentFilterService contentFilterService;
    private final AiService aiService;


    public PostService(PostRepository postRepository,
                       SubredditRepository subredditRepository,
                       AccountRepository accountRepository,
                       PostVoteRepository postVoteRepository,
                       CommentRepository commentRepository,
                       ImageUploadService imageUploadService,
                       ImageOptimizationService imageOptimizationService,
                       ImageEditService imageEditService,
                       @Qualifier("imageThreadPool") Executor imageThreadPool,
                       FastContentFilterService contentFilterService, AiService aiService) {
        this.postRepository = postRepository;
        this.subredditRepository = subredditRepository;
        this.accountRepository = accountRepository;
        this.postVoteRepository = postVoteRepository;
        this.commentRepository = commentRepository;
        this.imageUploadService = imageUploadService;
        this.imageOptimizationService = imageOptimizationService;
        this.imageEditService = imageEditService;
        this.imageThreadPool = imageThreadPool;
        this.contentFilterService = contentFilterService;
        this.aiService = aiService;
    }

    @Transactional
    public Post createPost(String title, String content, String authorUsername, String subredditName,
                          MultipartFile image, Integer filter) {
        validatePostTitle(title);
        validateContent(content);

        CompletableFuture<OptimizedImageResult> compressionFuture = null;
        String originalFilename = null;
        if (image != null && !image.isEmpty()) {
            imageOptimizationService.validateUpload(image);
            // Read and bound the upload while its request stream is still open; never materialize raw bytes.
            byte[] resizedBytes = imageOptimizationService.downscaleForFiltering(image);
            originalFilename = image.getOriginalFilename();
            compressionFuture = startImageProcessing(resizedBytes, image.getSize(), filter);
        }

        String formattedTitle = contentFilterService.sanitize(TextFormatterUtil.formatText(title));
        String formattedContent = contentFilterService.sanitize(TextFormatterUtil.formatText(content));

        if(formattedContent != null && !formattedContent.isBlank() && formattedContent.length() > 100){
            String aiSummary = aiService.generateSummary(title, content).join();
            String rawAIText = "/b{[AI Summary]:} " + "  \n\n" + aiSummary + " ──────────────────────── " + formattedContent + "\u3164".repeat(10);
            formattedContent = contentFilterService.sanitize(TextFormatterUtil.formatText(rawAIText));
        }

        // Censor title and content asynchronously using the AI service
        CompletableFuture<String> censorTitleFuture = aiService.censorText(formattedTitle);
        CompletableFuture<String> censorContentFuture = aiService.censorText(formattedContent);

        String censoredTitle = censorTitleFuture.join();
        String censoredContent = censorContentFuture.join();

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
        ImageStatus imageStatus = null;
        if (compressionFuture != null) {
            OptimizedImageResult optimizedImage = awaitImageCompression(compressionFuture);
            imageUrl = uploadOptimizedImage(optimizedImage, originalFilename, filter);
            censoredContent = appendImageOptimizationBadge(censoredContent, optimizedImage);
            imageStatus = ImageStatus.COMPLETED;
        }

        Post post = new Post(censoredTitle, censoredContent, author, subreddit, imageUrl, filter);
        post.setImageStatus(imageStatus);
        Post savedPost = postRepository.save(post);

        PostVote upvote = new PostVote(author, savedPost, PostVote.UPVOTE);
        postVoteRepository.save(upvote);
        subreddit.setPostCount(subreddit.getPostCount() + 1);

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
            String sanitizedTitle = contentFilterService.sanitize(request.getTitle().trim());
            String censoredTitle = aiService.censorText(sanitizedTitle).join();
            post.setTitle(censoredTitle);
        }
        if (request.getContent() != null) {
            validateContent(request.getContent());

            String formattedContent = contentFilterService.sanitize(TextFormatterUtil.formatText(request.getContent()));
            String formattedTitle = contentFilterService.sanitize(TextFormatterUtil.formatText(request.getTitle()));

            String censoredContent = aiService.censorText(formattedContent).join();
            String censoredTitle = aiService.censorText(formattedTitle).join();
            
            post.setContent(censoredContent);
            post.setTitle(censoredTitle);
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

    private CompletableFuture<OptimizedImageResult> startImageProcessing(byte[] resizedBytes, long originalSizeBytes, Integer filter) {
        try {
            return CompletableFuture.supplyAsync(
                    () -> processImage(resizedBytes, originalSizeBytes, filter),
                    imageThreadPool);
        } catch (RejectedExecutionException exception) {
            LOGGER.warn("Image thread pool rejected processing; running on the request thread. Reason: {}",
                    exception.getMessage());
            return CompletableFuture.completedFuture(
                    processImage(resizedBytes, originalSizeBytes, filter));
        }
    }

    private OptimizedImageResult processImage(byte[] resizedBytes, long originalSizeBytes, Integer filter) {
        // Only the bounded JPEG is retained after the request thread releases the upload stream.
        byte[] filteredBytes = filter == null
                ? resizedBytes
                : imageEditService.applyFilter(resizedBytes, filter);
        return imageOptimizationService.optimize(filteredBytes, "image/jpeg", originalSizeBytes);
    }

    private OptimizedImageResult awaitImageCompression(CompletableFuture<OptimizedImageResult> compressionFuture) {
        try {
            return compressionFuture.join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
            if (cause instanceof ImageSizeExceededException imageSizeExceededException) {
                throw imageSizeExceededException;
            }
            if (cause instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            LOGGER.error("Image compression failed: {}", cause.getMessage(), cause);
            throw new IllegalArgumentException("Image could not be processed.", cause);
        } catch (Exception exception) {
            compressionFuture.cancel(true);
            LOGGER.error("Image compression was interrupted: {}", exception.getMessage(), exception);
            throw new IllegalArgumentException("Image could not be processed.", exception);
        }
    }

    private String uploadOptimizedImage(OptimizedImageResult optimizedImage, String originalFilename, Integer filter) {
        try {
            String extension = "image/jpeg".equals(optimizedImage.getContentType())
                    ? ".jpg"
                    : extensionFromOriginalFilename(originalFilename);
            return imageUploadService.upload(
                    optimizedImage.getInputStream(),
                    optimizedImage.getOptimizedSizeBytes(),
                    optimizedImage.getContentType(),
                    extension,
                    null);
        } catch (ImageSizeExceededException | IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.error("Image upload to S3 failed: {}", exception.getMessage(), exception);
            throw new IllegalArgumentException("Image upload failed. Please try again.", exception);
        }
    }

    private String appendImageOptimizationBadge(String content, OptimizedImageResult optimizedImage) {
        if (!optimizedImage.isOptimized() || optimizedImage.getSavedPercentage() <= 0D) {
            return content;
        }

        long savedPercentage = Math.max(1L, Math.round(optimizedImage.getSavedPercentage()));
        String badge = String.format(
                Locale.US,
                "\u26A1 *Image optimized: %s \u2794 %.1f KB (-%d%% storage saved)*",
                formatOriginalImageSize(optimizedImage.getOriginalSizeBytes()),
                optimizedImage.getOptimizedSizeBytes() / 1024D,
                savedPercentage);
        return (content == null ? "" : content) + "\n\n" + badge;
    }

    private String formatOriginalImageSize(long sizeBytes) {
        if (sizeBytes >= 1024L * 1024L) {
            return String.format(Locale.US, "%.1f MB", sizeBytes / (1024D * 1024D));
        }
        return String.format(Locale.US, "%.1f KB", sizeBytes / 1024D);
    }

    private String extensionFromOriginalFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
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
        String imageStatus = post.getImageStatus() != null ? post.getImageStatus().name() : null;
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
            imageStatus = null;
            authorName = "[deleted]";
        }

        String subredditName = (post.getSubreddit() != null) ? post.getSubreddit().getName() : "unknown";

        return new PostDto.PostResponse(
                post.getId(),
                title,
                content,
                imageUrl,
                imageStatus,
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

package com.example.springreddit.service;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.exception.ForbiddenException;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import com.example.springreddit.repository.SubredditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final AccountRepository accountRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository,
                       SubredditRepository subredditRepository,
                       AccountRepository accountRepository,
                       PostVoteRepository postVoteRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.subredditRepository = subredditRepository;
        this.accountRepository = accountRepository;
        this.postVoteRepository = postVoteRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Post createPost(String title, String content, Long authorId, String subredditName) {
        if (authorId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: author ID is null");
            throw new IllegalArgumentException("Author ID cannot be null");
        }
        if (subredditName == null || subredditName.isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: subreddit name is blank");
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        validatePost(title, content);
        Account author = accountRepository.findById(authorId)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: author not found with ID: {}", authorId);
                    return new IllegalArgumentException("Author not found");
                });

        String name = normalizeSubredditName(subredditName);
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: subreddit not found: {}", name);
                    return new IllegalArgumentException("Subreddit not found");
                });

        Post post = new Post(title, content, author, subreddit);
        Post savedPost = postRepository.save(post);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Post created successfully with ID: {} in subreddit: {} by author ID: {}", savedPost.getId(), name, authorId);
        return savedPost;
    }

    @Transactional
    public Post createPost(String title, String content, String authorUsername, String subredditName,
                          MultipartFile image, Integer filter) throws IOException {
        validatePostTitle(title);
        validateContent(content);
        validateAuthor(authorUsername);
        validateSubreddit(subredditName);
        
        Account author = accountRepository.findByUsername(authorUsername)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: author not found with username: {}", authorUsername);
                    return new IllegalArgumentException("Author not found");
                });

        String name = normalizeSubredditName(subredditName);
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: subreddit not found: {}", name);
                    return new IllegalArgumentException("Subreddit not found");
                });

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = handleImageUpload(image);
        }

        Post post = new Post(title, content, author, subreddit, imageUrl, filter);
        Post savedPost = postRepository.save(post);
        
        PostVote upvote = new PostVote(author, savedPost, PostVote.UPVOTE);
        postVoteRepository.save(upvote);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Post created successfully with ID: {} in subreddit: {} by author: {}", savedPost.getId(), name, authorUsername);
        return savedPost;
    }

    public Post getPostById(UUID postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsBySubreddit(String subredditName) {
        if (subredditName == null || subredditName.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        return postRepository.findBySubreddit_Name(normalizeSubredditName(subredditName));
    }

    @Transactional
    public Post editPost(UUID postId, String newTitle, String newContent, Long accountId) {
        if (postId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit post failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (accountId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit post failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        validatePost(newTitle, newContent);
        Post post = getPostById(postId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit post failed: account not found with ID: {}", accountId);
                    return new IllegalArgumentException("Account not found");
                });

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(account.getId())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit post failed: unauthorized access to post ID: {} by account ID: {}", postId, accountId);
            throw new SecurityException("Only the post owner can edit it");
        }

        post.editPostContent(newTitle, newContent);
        postRepository.save(post);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Post edited successfully with ID: {} by account ID: {}", postId, accountId);
        return post;
    }

    @Transactional
    public Post updatePost(UUID id, UpdatePostRequest request, String currentUsername) {
        if (id == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Update post failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (currentUsername == null || currentUsername.isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Update post failed: missing authenticated username");
            throw new UnauthorizedException("Authentication is required");
        }
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Update post failed: request body is null");
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn(
                            "Update post failed: post not found with ID: {}", id);
                    return new ResourceNotFoundException("Post not found: " + id);
                });

        if (post.getAuthor() == null || !currentUsername.equals(post.getAuthor().getUsername())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "Update post failed: user '{}' is not the author of post ID: {}", currentUsername, id);
            throw new ForbiddenException("Only the post author can update it");
        }

        if (request.getTitle() != null) {
            validatePostTitle(request.getTitle());
            post.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            validateContent(request.getContent());
            post.setContent(request.getContent());
        }

        post.setUpdatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        com.example.springreddit.logging.CustomLogger.getInstance().info(
                "Post updated successfully with ID: {} by author: {}", id, currentUsername);
        return savedPost;
    }

    @Transactional(readOnly = true)
    public String resolveUserVote(Post post, String username) {
        if (post == null || username == null || username.isBlank()) {
            return null;
        }
        return accountRepository.findByUsername(username)
                .flatMap(account -> postVoteRepository.findByPostAndAccount(post, account))
                .map(vote -> vote.isUpvote() ? "up" : "down")
                .orElse("none");
    }

    @Transactional
    public void deletePost(UUID postId, Long accountId) {
        if (postId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (accountId == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: account ID is null");
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        Post post = getPostById(postId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: account not found with ID: {}", accountId);
                    return new IllegalArgumentException("Account not found");
                });

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(account.getId())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: unauthorized access to post ID: {} by account ID: {}", postId, accountId);
            throw new SecurityException("Only the post owner can delete it");
        }

        postRepository.delete(post);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Post deleted successfully with ID: {} by account ID: {}", postId, accountId);
    }

    @Transactional
    public void deletePost(UUID id, String currentUsername) {
        if (id == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (currentUsername == null || currentUsername.isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: missing authenticated username");
            throw new UnauthorizedException("Authentication is required");
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn(
                            "Delete post failed: post not found with ID: {}", id);
                    return new ResourceNotFoundException("Post not found: " + id);
                });

        if (post.getAuthor() == null || !currentUsername.equals(post.getAuthor().getUsername())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "Delete post failed: user '{}' is not the author of post ID: {}", currentUsername, id);
            throw new ForbiddenException("Only the post author can delete it");
        }

        postRepository.delete(post);
        com.example.springreddit.logging.CustomLogger.getInstance().info(
                "Post deleted successfully with ID: {} by author: {}", id, currentUsername);
    }

    public long countUpvotes(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        return postVoteRepository.countByPostAndVoteType(post, PostVote.UPVOTE);
    }

    public long countDownvotes(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        return postVoteRepository.countByPostAndVoteType(post, PostVote.DOWNVOTE);
    }

    private String normalizeSubredditName(String subredditName) {
        if (subredditName == null) {
            return null;
        }
        return subredditName.startsWith("r/") ? subredditName : "r/" + subredditName;
    }

    private void validatePost(String title, String content) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        if (title.length() > 150) throw new IllegalArgumentException("Title must not exceed 150 characters");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Content cannot be blank");
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

    private void validateImage(MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (image.getSize() > maxSize) {
                throw new IllegalArgumentException("Image size must not exceed 5MB");
            }
            String contentType = image.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new IllegalArgumentException("Image must be JPG or PNG");
            }
        }
    }

    private String handleImageUpload(MultipartFile image) throws IOException {
        validateImage(image);
        
        String uploadDir = "uploads";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String originalFilename = image.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String uniqueFilename = UUID.randomUUID() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        Files.copy(image.getInputStream(), filePath);
        
        com.example.springreddit.logging.CustomLogger.getInstance().info("Image uploaded successfully: {}", uniqueFilename);
        return "/uploads/" + uniqueFilename;
    }

    @Transactional(readOnly = true)
    public PostDto.PostResponse toPostResponse(Post post) {
        return toPostResponse(post, null);
    }

    @Transactional(readOnly = true)
    public PostDto.PostResponse toPostResponse(Post post, String userVote) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME;
        String authorName = (post.getAuthor() != null) ? post.getAuthor().getUsername() : "unknown";
        String subredditName = (post.getSubreddit() != null) ? post.getSubreddit().getName() : "unknown";
        long commentCount = commentRepository.countByPost_Id(post.getId());
        long score = countUpvotes(post) - countDownvotes(post);

        return new PostDto.PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getFilter(),
                authorName,
                subredditName,
                countUpvotes(post),
                countDownvotes(post),
                score,
                commentCount,
                userVote,
                post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : null,
                post.getUpdatedAt() != null ? post.getUpdatedAt().format(formatter) : null
        );
    }
}

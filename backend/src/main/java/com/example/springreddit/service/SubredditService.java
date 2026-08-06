package com.example.springreddit.service;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.SubredditRepository;
import com.example.springreddit.repository.SubredditSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubredditService {

    @Autowired
    private SubredditRepository subredditRepository;

    @Autowired
    private AccountRepository accountRepository;

    public Subreddit createSubreddit(SubredditDto.CreateSubredditRequest request) {
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null || 
            !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: user not authenticated");
            throw new SecurityException("User not authenticated");
        }
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        String normalizedName = normalizeName(request.name());
        if (subredditRepository.existsByName(normalizedName)) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: subreddit already exists with name: {}", normalizedName);
            throw new IllegalArgumentException("Subreddit already exists");
        }

        Account creator = accountRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: creator not found with username: {}", currentUsername);
                    return new IllegalArgumentException("Creator not found");
                });

        Subreddit subreddit = new Subreddit();
        subreddit.setName(request.name());
        subreddit.setDisplayName(request.displayName());
        subreddit.setDescription(request.description());
        subreddit.setIconURL(request.iconUrl());
        subreddit.setCreator(creator);

        Subreddit savedSubreddit = subredditRepository.save(subreddit);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit created successfully with ID: {} and name: {} by user: {}", savedSubreddit.getId(), normalizedName, currentUsername);
        return savedSubreddit;
    }

    public List<SubredditSummary> getAllSubredditSummaries() {
        return subredditRepository.findAllSummaries();
    }

    public List<Subreddit> getSubredditsByCreatorUsername(String username) {
        return subredditRepository.findByCreator_Username(username);
    }

    public Subreddit getSubredditByName(String subredditName) {
        if (subredditName == null || subredditName.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String searchName = normalizeName(subredditName);
        return subredditRepository.findByName(searchName)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));
    }

    public List<PostDto.PostResponse> getPostsBySubredditName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        String searchName = normalizeName(name);
        Subreddit subreddit = subredditRepository.findByName(searchName)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));
        
        return subreddit.getPosts().stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    public Subreddit editSubreddit(String name, SubredditDto.EditSubredditRequest request) {
        if (name == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: subreddit name is null");
            throw new IllegalArgumentException("Subreddit name cannot be null");
        }
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }

        // Extract current user from JWT
        if (SecurityContextHolder.getContext().getAuthentication() == null || 
            !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: user not authenticated");
            throw new SecurityException("User not authenticated");
        }
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Account currentUser = accountRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: user not found with username: {}", currentUsername);
                    return new IllegalArgumentException("User not found");
                });

        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: subreddit not found with name: {}", name);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if (subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(currentUser.getId())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: unauthorized access to subreddit name: {} by user: {}", name, currentUsername);
            throw new SecurityException("Only the subreddit creator can edit it");
        }

        // Only update fields that are provided in the request
        if (request.displayName() != null) {
            subreddit.setDisplayName(request.displayName());
        }
        if (request.description() != null) {
            subreddit.setDescription(request.description());
        }
        if (request.iconUrl() != null) {
            subreddit.setIconURL(request.iconUrl());
        }

        subredditRepository.save(subreddit);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit edited successfully with name: {} by user: {}", name, currentUsername);
        return subreddit;
    }

    public void deleteSubreddit(String name) {
        if (name == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit name is null");
            throw new IllegalArgumentException("Subreddit name cannot be null");
        }

        // Extract current user from JWT
        if (SecurityContextHolder.getContext().getAuthentication() == null || 
            !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: user not authenticated");
            throw new SecurityException("User not authenticated");
        }
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Account currentUser = accountRepository.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: user not found with username: {}", currentUsername);
                    return new IllegalArgumentException("User not found");
                });

        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit not found with name: {}", name);
                    return new IllegalArgumentException("Subreddit not found");
                });

        if (subreddit.getCreator() == null || !subreddit.getCreator().getId().equals(currentUser.getId())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: unauthorized access to subreddit name: {} by user: {}", name, currentUsername);
            throw new SecurityException("Only the subreddit creator can delete it");
        }

        // Check if subreddit has posts - cannot delete if it has posts
        if (subreddit.getPostCount() > 0) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: subreddit {} has posts", name);
            throw new IllegalArgumentException("Cannot delete subreddit with posts");
        }

        subredditRepository.deleteByName(name);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Subreddit deleted successfully with name: {} by user: {}", name, currentUsername);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subreddit name cannot be blank");
        }
        return name.trim();
    }

    private PostDto.PostResponse mapPostToResponse(Post post) {
        String userVote = null;
        // Calculate upvotes and downvotes from votes list
        long upvotes = post.getVotes().stream()
                .filter(vote -> vote.getVoteType() == 1) // Assuming 1 = upvote
                .count();
        long downvotes = post.getVotes().stream()
                .filter(vote -> vote.getVoteType() == -1) // Assuming -1 = downvote
                .count();
        
        return new PostDto.PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getFilter(),
                post.getAuthor().getUsername(),
                post.getSubreddit().getName(),
                upvotes,
                downvotes,
                upvotes - downvotes,
                post.getComments().size(),
                userVote,
                post.getCreatedAt().toString(),
                post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null
        );
    }
}

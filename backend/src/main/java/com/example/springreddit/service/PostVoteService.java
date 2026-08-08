package com.example.springreddit.service;

import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PostVoteService {

    private final PostVoteRepository postVoteRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public PostVoteService(PostVoteRepository postVoteRepository,
                           PostRepository postRepository,
                           AccountRepository accountRepository) {
        this.postVoteRepository = postVoteRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public VoteResponse voteOnPost(UUID postId, String currentUsername, String voteType) {
        if (postId == null) {
            LOGGER.warn("Vote failed: post ID is null");
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (currentUsername == null || currentUsername.isBlank()) {
            LOGGER.warn("Vote failed: missing authenticated username");
            throw new UnauthorizedException("Authentication is required");
        }
        if (voteType == null || (!voteType.equals("up") && !voteType.equals("down") && !voteType.equals("none"))) {
            LOGGER.warn("Vote failed: invalid voteType '{}'", voteType);
            throw new IllegalArgumentException("Vote type must be 'up', 'down', or 'none'");
        }

        Long accountId = accountRepository.findIdByUsername(currentUsername)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Vote failed: account not found with username: {}", currentUsername);
                    return new UnauthorizedException("User not found");
                });

        Optional<Short> existingVoteType =
                postVoteRepository.findVoteTypeByPostIdAndAccountId(postId, accountId);

        switch (voteType) {
            case "none" -> {
                if (existingVoteType.isEmpty() && !postRepository.existsById(postId)) {
                    LOGGER.warn("Vote failed: post not found with ID: {}", postId);
                    throw new ResourceNotFoundException("Post not found: " + postId);
                }
                if (existingVoteType.isPresent()) {
                    postVoteRepository.deleteByPostIdAndAccountId(postId, accountId);
                    LOGGER.info("Vote removed for post ID: {} by user: {}", postId, currentUsername);
                }
                return buildVoteResponse(postId, "none");
            }
            case "up", "down" -> {
                short newVoteType = "up".equals(voteType) ? PostVote.UPVOTE : PostVote.DOWNVOTE;
                if (existingVoteType.isEmpty()) {
                    if (!postRepository.existsById(postId)) {
                        LOGGER.warn("Vote failed: post not found with ID: {}", postId);
                        throw new ResourceNotFoundException("Post not found: " + postId);
                    }
                    postVoteRepository.insertVote(accountId, postId, newVoteType);
                    LOGGER.info(
                            "Vote {} for post ID: {} by user: {}",
                            voteType.equals("up") ? "upvoted" : "downvoted",
                            postId,
                            currentUsername);
                } else if (existingVoteType.get() != newVoteType) {
                    postVoteRepository.updateVoteType(postId, accountId, newVoteType);
                    LOGGER.info(
                            "Vote {} for post ID: {} by user: {}",
                            voteType.equals("up") ? "upvoted" : "downvoted",
                            postId,
                            currentUsername);
                }
                return buildVoteResponse(postId, voteType);
            }
            default -> throw new IllegalArgumentException("Invalid vote type: " + voteType);
        }
    }

    private VoteResponse buildVoteResponse(UUID postId, String userVote) {
        long upvotes = 0L;
        long downvotes = 0L;
        for (Object[] row : postVoteRepository.countGroupedByPostId(postId)) {
            short type = ((Number) row[0]).shortValue();
            long count = ((Number) row[1]).longValue();
            if (type == PostVote.UPVOTE) {
                upvotes = count;
            } else if (type == PostVote.DOWNVOTE) {
                downvotes = count;
            }
        }
        return new VoteResponse(upvotes, downvotes, upvotes - downvotes, userVote);
    }
}

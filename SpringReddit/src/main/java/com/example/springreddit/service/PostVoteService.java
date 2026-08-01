package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PostVoteService {

    private final PostVoteRepository postVoteRepository;
    private final PostRepository postRepository;

    public PostVoteService(PostVoteRepository postVoteRepository, PostRepository postRepository) {
        this.postVoteRepository = postVoteRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public String vote(Long postId, Account currentAccount, boolean isUpvote, int choice) {
        validateVoteRequest(postId, currentAccount, choice);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with ID " + postId + " does not exist."));

        Optional<PostVote> existingVoteOpt = postVoteRepository.findByPostAndAccount(post, currentAccount);
        String voteTypeStr = isUpvote ? "upvote" : "downvote";

        if (choice == 1) {
            if (existingVoteOpt.isPresent()) {
                PostVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    return "You have already voted! You cannot " + voteTypeStr + " twice.";
                }
                existingVote.setUpvote(isUpvote);
                postVoteRepository.save(existingVote);
                return "Vote changed to " + voteTypeStr + " successfully.";
            }
            postVoteRepository.save(new PostVote(isUpvote, post, currentAccount));
            return voteTypeStr.substring(0, 1).toUpperCase() + voteTypeStr.substring(1) + " added successfully.";
        }

        if (choice == 2) {
            if (existingVoteOpt.isEmpty()) {
                return "Error: You have not voted on this post, so you cannot remove a vote";
            }
            PostVote existingVote = existingVoteOpt.get();
            if (existingVote.isUpvote() != isUpvote) {
                return "Error: You are trying to remove an " + voteTypeStr + ", but you cast the opposite vote";
            }
            postVoteRepository.delete(existingVote);
            return voteTypeStr.substring(0, 1).toUpperCase() + voteTypeStr.substring(1) + " removed successfully";
        }

        return "Invalid choice";
    }

    public long countUpvotes(Long postId) {
        return postVoteRepository.countByPost_IdAndVoteType(postId, PostVote.UPVOTE);
    }

    public long countDownvotes(Long postId) {
        return postVoteRepository.countByPost_IdAndVoteType(postId, PostVote.DOWNVOTE);
    }

    public int currentVote(Long postId, Long accountId) {
        return postVoteRepository.findByPost_IdAndAccount_Id(postId, accountId)
                .map(vote -> (int) vote.getVoteType())
                .orElse(0);
    }

    private void validateVoteRequest(Long postId, Account account, int choice) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (choice != 1 && choice != 2) {
            throw new IllegalArgumentException("Choice must be 1 or 2");
        }
    }
}

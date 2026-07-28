package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Mirrors CLI {@code PostVoteServiceImpl} vote semantics (choice 1 = add/change, 2 = remove).
 */
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
}

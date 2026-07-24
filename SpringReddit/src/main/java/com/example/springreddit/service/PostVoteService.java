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
    public void vote(Long postId, Account currentAccount, boolean isUpvote) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Postarea nu a fost găsită cu ID-ul: " + postId));

        Optional<PostVote> existingVoteOpt = postVoteRepository.findByPostAndAccount(post, currentAccount);

        if (existingVoteOpt.isPresent()) {
            PostVote existingVote = existingVoteOpt.get();

            if (existingVote.isUpvote() == isUpvote) {
                postVoteRepository.delete(existingVote);
            }
            else {
                PostVote newVote = new PostVote(isUpvote, post, currentAccount);

                postVoteRepository.save(newVote);
            }
        }

    }
}
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
    public void vote(Long postId, Long accountId, int voteDirection) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("Post not found"));

        Account account = null;
        Optional<PostVote> existingVoteOpt =  postVoteRepository.findByPostAndAccount(post, account);
        if(existingVoteOpt.isPresent()) {
            PostVote existingVote = existingVoteOpt.get();
            if (existingVote.getVoteDirection() == voteDirection) {
                postVoteRepository.delete(existingVote);

            } else {
                existingVote.setVoteDirection(voteDirection);
                postVoteRepository.save(existingVote);
            }
        }
            else
            {
                PostVote newVote = new PostVote(post,account,voteDirection);
                postVoteRepository.save(newVote);
            }
        }
    }


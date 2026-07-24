package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    Optional<PostVote> findByPostAndAccount(Post post, Account account);
    long countByPostAndIsUpvote(Post post, boolean isUpvote);
}
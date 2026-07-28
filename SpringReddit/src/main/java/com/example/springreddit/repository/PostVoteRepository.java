package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.PostVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, PostVoteId> {

    Optional<PostVote> findByPostAndAccount(Post post, Account account);

    long countByPostAndVoteType(Post post, short voteType);
}

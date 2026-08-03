package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.PostVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, PostVoteId> {

    Optional<PostVote> findByPostAndAccount(Post post, Account account);

    Optional<PostVote> findByPost_IdAndAccount_Id(Long postId, Long accountId);

    long countByPostAndVoteType(Post post, short voteType);

    long countByPost_IdAndVoteType(Long postId, short voteType);

    /* Currently unused but useful methodss
    void deleteByPostAndAccount(Post post, Account account);

    List<PostVote> findByPost_Id(Long postId);

     */
}

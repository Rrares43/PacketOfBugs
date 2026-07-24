package com.example.springreddit.repository;

import com.example.springreddit.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {
    Optional<PostVote> findByPostAndAccount(Post post, Account account);
}

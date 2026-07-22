package com.example.springreddit.repository;

import com.example.springreddit.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubredditRepository extends JpaRepository<Subreddit, Long> {
    Optional<Subreddit> findBySubredditName(String subredditName);
    boolean existsBySubredditName(String subredditName);
}

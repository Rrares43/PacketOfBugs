package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Long> {

    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);

    List<Subreddit> findByCreator(Account creator);

    List<Subreddit> findByCreator_Username(String username);

    void deleteByName(String name);
}

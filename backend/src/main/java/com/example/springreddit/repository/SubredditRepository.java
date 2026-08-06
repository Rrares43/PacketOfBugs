package com.example.springreddit.repository;

import com.example.springreddit.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, UUID> {

    Optional<Subreddit> findByName(String name);

    void deleteByName(String name);

    boolean existsByName(String name);

    List<Subreddit> findByCreator_Username(String username);

    @Query("SELECT s FROM Subreddit s")
    List<SubredditSummary> findAllSummaries();

    /* Currently unused but useful method
    List<Subreddit> findByCreator(Account creator);

    void deleteByName(String name);

     */
}

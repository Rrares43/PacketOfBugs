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

    @Query("""
            select s.id as id, s.name as name, s.description as description,
                   c.id as creatorId, c.username as creatorUsername,
                   s.createdAt as createdAt, count(p.id) as postCount
            from Subreddit s
            left join s.creator c
            left join Post p on p.subreddit = s
            group by s.id, s.name, s.description, c.id, c.username, s.createdAt
            order by s.name
            """)
    List<SubredditSummary> findAllSummaries();

    /* Currently unused but useful method
    List<Subreddit> findByCreator(Account creator);

    void deleteByName(String name);

     */
}

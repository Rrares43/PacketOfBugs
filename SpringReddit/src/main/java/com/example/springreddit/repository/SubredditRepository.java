package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}

package com.example.springreddit.repository;

import com.example.springreddit.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.subreddit
            ORDER BY p.createdAt DESC
            """)
    List<Post> findAllWithAuthorAndSubreddit();

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.subreddit
            WHERE p.subreddit.name = :name
            ORDER BY p.createdAt DESC
            """)
    List<Post> findBySubredditNameWithAuthorAndSubreddit(@Param("name") String name);

    @Query("""
            SELECT p FROM Post p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.subreddit
            WHERE p.id = :id
            """)
    Optional<Post> findByIdWithAuthorAndSubreddit(@Param("id") UUID id);

    List<Post> findBySubreddit_Name(String name);
}

package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findBySubreddit(Subreddit subreddit);

    List<Post> findBySubreddit_Name(String name);

    List<Post> findByAuthor(Account author);

    List<Post> findByAuthor_Username(String username);

    void deleteBySubreddit_Name(String name);
}

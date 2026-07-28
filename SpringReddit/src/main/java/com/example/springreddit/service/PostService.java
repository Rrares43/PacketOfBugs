package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.PostVoteRepository;
import com.example.springreddit.repository.SubredditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final AccountRepository accountRepository;
    private final PostVoteRepository postVoteRepository;

    public PostService(PostRepository postRepository,
                       SubredditRepository subredditRepository,
                       AccountRepository accountRepository,
                       PostVoteRepository postVoteRepository) {
        this.postRepository = postRepository;
        this.subredditRepository = subredditRepository;
        this.accountRepository = accountRepository;
        this.postVoteRepository = postVoteRepository;
    }

    @Transactional
    public Post createPost(String title, String content, Long authorId, String subredditName) {
        Account author = accountRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        String name = normalizeSubredditName(subredditName);
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Subreddit not found"));

        Post post = new Post(title, content, author, subreddit);
        return postRepository.save(post);
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsBySubreddit(String subredditName) {
        return postRepository.findBySubreddit_Name(normalizeSubredditName(subredditName));
    }

    @Transactional
    public Post editPost(Long postId, String newTitle, String newContent, Long accountId) {
        Post post = getPostById(postId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(account.getId())) {
            throw new SecurityException("Only the post owner can edit it");
        }

        post.editPostContent(newTitle, newContent);
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long accountId) {
        Post post = getPostById(postId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(account.getId())) {
            throw new SecurityException("Only the post owner can delete it");
        }

        postRepository.delete(post);
    }

    public long countUpvotes(Post post) {
        return postVoteRepository.countByPostAndVoteType(post, PostVote.UPVOTE);
    }

    public long countDownvotes(Post post) {
        return postVoteRepository.countByPostAndVoteType(post, PostVote.DOWNVOTE);
    }

    private String normalizeSubredditName(String subredditName) {
        if (subredditName == null) {
            return null;
        }
        return subredditName.startsWith("r/") ? subredditName : "r/" + subredditName;
    }
}

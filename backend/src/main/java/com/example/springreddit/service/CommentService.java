package com.example.springreddit.service;

import com.example.springreddit.dto.CommentDto.CommentRequest;
import com.example.springreddit.dto.CommentDto.CommentResponse;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.CommentVoteRepository;
import com.example.springreddit.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public CommentService(CommentRepository commentRepository,
                          CommentVoteRepository commentVoteRepository,
                          PostRepository postRepository,
                          AccountRepository accountRepository) {
        this.commentRepository = commentRepository;
        this.commentVoteRepository = commentVoteRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(UUID commentId) {
        Comment comment = findComment(commentId);
        return toResponse(comment, null);
    }

    @Transactional
    public CommentResponse createComment(UUID postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        Account author = accountRepository.findByUsername(request.author())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + request.author()));

        Comment comment = new Comment(request.content().trim(), author, post);
        if (request.parentId() == null) {
            post.addComment(comment);
        } else {
            Comment parent = commentRepository.findByIdAndPost_Id(request.parentId(), postId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent comment not found on post: " + request.parentId()));
            if (parent.isDeleted()) {
                throw new IllegalArgumentException("Replies cannot be added to a deleted comment");
            }
            parent.addReply(comment);
        }

        Comment saved = commentRepository.save(comment);
        commentVoteRepository.save(new CommentVote(saved, author, CommentVote.UPVOTE));
        LOGGER.info("Comment created with ID: {} on post: {} by: {}", saved.getId(), postId, author.getUsername());
        return toResponse(saved, author);
    }

    private Comment findComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private CommentResponse toResponse(Comment comment, Account currentUser) {
        long upvotes = commentVoteRepository.countByComment_IdAndVoteType(comment.getId(), CommentVote.UPVOTE);
        long downvotes = commentVoteRepository.countByComment_IdAndVoteType(comment.getId(), CommentVote.DOWNVOTE);
        String userVote = currentUser == null ? null : commentVoteRepository
                .findByCommentAndAccount(comment, currentUser)
                .map(vote -> vote.isUpvote() ? "up" : "down")
                .orElse(null);
        List<CommentResponse> replies = comment.getReplies().stream()
                .map(reply -> toResponse(reply, currentUser))
                .toList();

        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                comment.isDeleted() ? "[deleted]" : comment.getContent(),
                comment.isDeleted() ? "[deleted]" : comment.getAuthor().getUsername(),
                upvotes,
                downvotes,
                upvotes - downvotes,
                userVote,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies
        );
    }
}

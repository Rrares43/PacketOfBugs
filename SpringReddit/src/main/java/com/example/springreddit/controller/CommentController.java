package com.example.springreddit.controller;

import com.example.springreddit.dto.CommentDto;
import com.example.springreddit.dto.VoteDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.service.AccountService;
import com.example.springreddit.service.CommentService;
import com.example.springreddit.service.CommentVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {

    private final CommentService commentService;
    private final CommentVoteService commentVoteService;
    private final AccountService accountService;

    public CommentController(CommentService commentService,
                             CommentVoteService commentVoteService,
                             AccountService accountService) {
        this.commentService = commentService;
        this.commentVoteService = commentVoteService;
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<CommentDto.CommentResponse> comments = commentService.getTopLevelComments(postId).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @Valid @RequestBody CommentDto.CreateCommentRequest request) {
        try {
            Account author = accountService.getById(request.getAuthorId());
            Comment comment = commentService.comment(postId, request.getContent(), author);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(comment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/{parentCommentId}/replies", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reply(@PathVariable Long postId,
                                   @PathVariable Long parentCommentId,
                                   @Valid @RequestBody CommentDto.ReplyCommentRequest request) {
        try {
            Account author = accountService.getById(request.getAuthorId());
            Comment reply = commentService.replyToComment(postId, parentCommentId, request.getContent(), author);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reply));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editComment(@PathVariable Long postId,
                                         @PathVariable Long commentId,
                                         @Valid @RequestBody CommentDto.EditCommentRequest request) {
        try {
            Account editor = accountService.getById(request.getAccountId());
            Comment comment = commentService.editComment(postId, commentId, request.getContent(), editor);
            return ResponseEntity.ok(toResponse(comment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping(value = "/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @RequestBody CommentDto.DeleteCommentRequest request) {
        try {
            Account deleter = accountService.getById(request.getAccountId());
            commentService.deleteComment(postId, commentId, deleter);
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping(value = "/{commentId}/votes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> vote(@PathVariable Long postId,
                                  @PathVariable Long commentId,
                                  @Valid @RequestBody VoteDto.VoteRequest request) {
        try {
            Account account = accountService.getById(request.getAccountId());
            String message = commentVoteService.vote(postId, commentId, account, request.isUpvote(), request.getChoice());

            VoteDto.VoteResponse response = new VoteDto.VoteResponse();
            response.setMessage(message);
            response.setUpvotes(commentVoteService.countUpvotes(commentId));
            response.setDownvotes(commentVoteService.countDownvotes(commentId));
            response.setCurrentUserVote(commentVoteService.currentVote(commentId, request.getAccountId()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private CommentDto.CommentResponse toResponse(Comment comment) {
        CommentDto.CommentResponse response = new CommentDto.CommentResponse();
        response.setId(comment.getId());
        response.setDeleted(comment.isDeleted());
        response.setDeletedAt(comment.getDeletedAt());
        response.setContent(comment.isDeleted() ? "[deleted]" : comment.getContent());
        if (comment.isDeleted()) {
            response.setAuthorUsername("[deleted]");
        } else if (comment.getAuthor() != null) {
            response.setAuthorId(comment.getAuthor().getId());
            response.setAuthorUsername(comment.getAuthor().getUsername());
        }
        if (comment.getPost() != null) {
            response.setPostId(comment.getPost().getId());
        }
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpvotes(commentVoteService.countUpvotes(comment.getId()));
        response.setDownvotes(commentVoteService.countDownvotes(comment.getId()));
        response.setReplies(comment.getReplies().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
        return response;
    }
}

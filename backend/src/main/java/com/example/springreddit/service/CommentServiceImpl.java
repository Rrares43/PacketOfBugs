package com.example.springreddit.service;

import com.example.springreddit.dto.CommentResponse;
import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.VoteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              VoteRepository voteRepository,
                              PostRepository postRepository,
                              AccountRepository accountRepository) {
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getComment(UUID commentId) {
        Comment comment = commentRepository.findByIdWithReplies(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        return toResponse(comment, currentAccountOrNull());
    }

    @Override
    @Transactional
    public CommentResponse createComment(UUID postId, CreateCommentRequest request) {
        Account author = requireCurrentAccount();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

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
        voteRepository.save(new CommentVote(saved, author, CommentVote.UPVOTE));
        LOGGER.info("Comment created with ID: {} on post: {} by: {}",
                saved.getId(), postId, author.getUsername());
        return toResponse(saved, author);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest request) {
        Account currentAccount = requireCurrentAccount();
        Comment comment = findComment(commentId);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Cannot update a deleted comment");
        }
        if (!comment.isAuthoredBy(currentAccount)) {
            LOGGER.warn("Unauthorized comment update for ID: {} by account ID: {}",
                    commentId, currentAccount.getId());
            throw new AccessDeniedException("Only the comment author can update it");
        }

        comment.editContent(request.content().trim());
        Comment saved = commentRepository.save(comment);
        LOGGER.info("Comment updated with ID: {}", saved.getId());
        return toResponse(saved, currentAccount);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Account currentAccount = requireCurrentAccount();
        Comment comment = findComment(commentId);
        if (!comment.isAuthoredBy(currentAccount) && !currentAuthenticationIsAdmin()) {
            LOGGER.warn("Unauthorized comment deletion for ID: {} by account ID: {}",
                    commentId, currentAccount.getId());
            throw new AccessDeniedException("Only the comment author or an administrator can delete it");
        }
        if (!comment.isDeleted()) {
            comment.softDelete();
            commentRepository.save(comment);
        }
        LOGGER.info("Comment deleted with ID: {} by account ID: {}", commentId, currentAccount.getId());
    }

    @Override
    @Transactional
    public CommentResponse vote(UUID commentId, VoteRequest request) {
        Account currentAccount = requireCurrentAccount();
        Comment comment = findComment(commentId);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Deleted comments cannot be voted on");
        }

        CommentVote existingVote = voteRepository
                .findByComment_IdAndAccount_Id(commentId, currentAccount.getId())
                .orElse(null);
        switch (request.voteType()) {
            case "up" -> saveVote(comment, currentAccount, existingVote, CommentVote.UPVOTE);
            case "down" -> saveVote(comment, currentAccount, existingVote, CommentVote.DOWNVOTE);
            case "none" -> {
                if (existingVote != null) {
                    voteRepository.delete(existingVote);
                }
            }
            default -> throw new IllegalArgumentException("Vote type must be up, down, or none");
        }
        voteRepository.flush();

        LOGGER.info("Comment vote set to {} for ID: {} by account ID: {}",
                request.voteType(), commentId, currentAccount.getId());
        return toResponse(comment, currentAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found: " + postId);
        }
        return commentRepository.findByPost_IdAndParentCommentIsNullOrderByCreatedAtAscIdAsc(postId).stream()
                .map(comment -> toResponse(comment, currentAccountOrNull()))
                .toList();
    }


    private void saveVote(Comment comment, Account account, CommentVote existingVote, short voteType) {
        if (existingVote == null) {
            voteRepository.save(new CommentVote(comment, account, voteType));
        } else if (existingVote.getVoteType() != voteType) {
            existingVote.setVoteType(voteType);
            voteRepository.save(existingVote);
        }
    }

    private Comment findComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private Account requireCurrentAccount() {
        Account account = currentAccountOrNull();
        if (account == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required");
        }
        return account;
    }

    private Account currentAccountOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return accountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "Authenticated account no longer exists"));
    }

    private boolean currentAuthenticationIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority) || "ADMIN".equals(authority));
    }

    private CommentResponse toResponse(Comment comment, Account currentUser) {
        long upvotes = voteRepository.countByComment_IdAndVoteType(comment.getId(), CommentVote.UPVOTE);
        long downvotes = voteRepository.countByComment_IdAndVoteType(comment.getId(), CommentVote.DOWNVOTE);
        String userVote = currentUser == null ? null : voteRepository
                .findByComment_IdAndAccount_Id(comment.getId(), currentUser.getId())
                .map(vote -> vote.isUpvote() ? "up" : "down")
                .orElse("none");
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

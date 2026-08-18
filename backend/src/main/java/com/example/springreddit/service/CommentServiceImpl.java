package com.example.springreddit.service;

import com.example.springreddit.dto.CommentResponse;
import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;
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
import com.example.springreddit.util.TextFormatterUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final FastContentFilterService contentFilterService;
    private final AiService aiService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              VoteRepository voteRepository,
                              PostRepository postRepository,
                              AccountRepository accountRepository,
                              FastContentFilterService contentFilterService,
                              AiService aiService) {
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
        this.contentFilterService = contentFilterService;
        this.aiService = aiService;
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

        String formattedContent = contentFilterService.sanitize(
                TextFormatterUtil.formatText(request.content().trim()));
        
        String censoredContent = aiService.censorText(formattedContent).join();

        Comment comment = new Comment(censoredContent, author, post);
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

        String formattedContent = contentFilterService.sanitize(
                TextFormatterUtil.formatText(request.content().trim()));
        
        String censoredContent = aiService.censorText(formattedContent).join();

        comment.editContent(censoredContent);
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
    public VoteResponse vote(UUID commentId, VoteRequest request) {
        Account currentAccount = requireCurrentAccount();
        Comment comment = findComment(commentId);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Deleted comments cannot be voted on");
        }

        CommentVote existingVote = voteRepository
                .findByComment_IdAndAccount_Id(commentId, currentAccount.getId())
                .orElse(null);
        String userVote = request.voteType();
        switch (userVote) {
            case "up" -> applyCommentVote(comment, currentAccount, existingVote, CommentVote.UPVOTE);
            case "down" -> applyCommentVote(comment, currentAccount, existingVote, CommentVote.DOWNVOTE);
            case "none" -> {
                if (existingVote != null) {
                    voteRepository.delete(existingVote);
                }
            }
            default -> throw new IllegalArgumentException("Vote type must be up, down, or none");
        }

        LOGGER.info("Comment vote set to {} for ID: {} by account ID: {}",
                userVote, commentId, currentAccount.getId());
        return buildVoteResponse(commentId, userVote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found: " + postId);
        }

        List<Comment> comments = commentRepository.findAllByPostIdWithDetails(postId);
        if (comments.isEmpty()) {
            return List.of();
        }

        Account currentUser = currentAccountOrNull();
        List<UUID> commentIds = comments.stream().map(Comment::getId).toList();
        Map<UUID, long[]> voteCounts = loadCommentVoteCounts(commentIds);
        Map<UUID, String> userVotes = loadUserVotes(commentIds, currentUser);

        Map<UUID, List<Comment>> childrenByParentId = new LinkedHashMap<>();
        List<Comment> roots = new ArrayList<>();
        for (Comment comment : comments) {
            Comment parent = comment.getParentComment();
            if (parent == null) {
                roots.add(comment);
            } else {
                childrenByParentId
                        .computeIfAbsent(parent.getId(), id -> new ArrayList<>())
                        .add(comment);
            }
        }

        return roots.stream()
                .map(comment -> toResponse(comment, childrenByParentId, voteCounts, userVotes))
                .toList();
    }

    private void applyCommentVote(Comment comment, Account account, CommentVote existingVote, short voteType) {
        if (existingVote == null) {
            voteRepository.save(new CommentVote(comment, account, voteType));
        } else if (existingVote.getVoteType() != voteType) {
            existingVote.setVoteType(voteType);
            voteRepository.save(existingVote);
        }
    }

    private VoteResponse buildVoteResponse(UUID commentId, String userVote) {
        long upvotes = 0L;
        long downvotes = 0L;
        for (Object[] row : voteRepository.countGroupedByCommentId(commentId)) {
            short type = ((Number) row[0]).shortValue();
            long count = ((Number) row[1]).longValue();
            if (type == CommentVote.UPVOTE) {
                upvotes = count;
            } else if (type == CommentVote.DOWNVOTE) {
                downvotes = count;
            }
        }
        return new VoteResponse(upvotes, downvotes, upvotes - downvotes, userVote);
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

        return buildCommentResponse(comment, upvotes, downvotes, userVote, replies);
    }

    private CommentResponse toResponse(
            Comment comment,
            Map<UUID, List<Comment>> childrenByParentId,
            Map<UUID, long[]> voteCounts,
            Map<UUID, String> userVotes) {
        long[] counts = voteCounts.getOrDefault(comment.getId(), new long[]{0L, 0L});
        List<CommentResponse> replies = childrenByParentId
                .getOrDefault(comment.getId(), List.of())
                .stream()
                .map(reply -> toResponse(reply, childrenByParentId, voteCounts, userVotes))
                .toList();
        return buildCommentResponse(
                comment,
                counts[0],
                counts[1],
                userVotes.get(comment.getId()),
                replies);
    }

    private CommentResponse buildCommentResponse(
            Comment comment,
            long upvotes,
            long downvotes,
            String userVote,
            List<CommentResponse> replies) {

        String content = comment.isDeleted() ? "[deleted]" : comment.getContent();

        String authorName = "unknown";

        if (comment.isDeleted()) {
            authorName = "[deleted]";
        } else if (comment.getAuthor() != null) {
            authorName = comment.getAuthor().isDeleted() ? "[deleted]" : comment.getAuthor().getUsername();
        }

        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                content,
                authorName,
                upvotes,
                downvotes,
                upvotes - downvotes,
                userVote,
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                replies
        );
    }

    private Map<UUID, long[]> loadCommentVoteCounts(Collection<UUID> commentIds) {
        Map<UUID, long[]> counts = new HashMap<>();
        for (Object[] row : voteRepository.countGroupedByCommentIds(commentIds)) {
            UUID commentId = (UUID) row[0];
            short voteType = ((Number) row[1]).shortValue();
            long count = ((Number) row[2]).longValue();
            long[] bucket = counts.computeIfAbsent(commentId, id -> new long[]{0L, 0L});
            if (voteType == CommentVote.UPVOTE) {
                bucket[0] = count;
            } else if (voteType == CommentVote.DOWNVOTE) {
                bucket[1] = count;
            }
        }
        return counts;
    }

    private Map<UUID, String> loadUserVotes(Collection<UUID> commentIds, Account currentUser) {
        Map<UUID, String> userVotes = new HashMap<>();
        if (currentUser == null) {
            return userVotes;
        }

        for (Object[] row : voteRepository.findVoteTypesByCommentIdsAndAccountId(
                commentIds, currentUser.getId())) {
            UUID commentId = (UUID) row[0];
            short voteType = ((Number) row[1]).shortValue();
            userVotes.put(commentId, voteType == CommentVote.UPVOTE ? "up" : "down");
        }

        for (UUID commentId : commentIds) {
            userVotes.putIfAbsent(commentId, "none");
        }
        return userVotes;
    }
}

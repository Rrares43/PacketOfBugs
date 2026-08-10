package com.example.springreddit.service;

import com.example.springreddit.dto.CommentResponse;
import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse getComment(UUID commentId);

    CommentResponse createComment(UUID postId, CreateCommentRequest request);

    CommentResponse updateComment(UUID commentId, UpdateCommentRequest request);

    void deleteComment(UUID commentId);

    VoteResponse vote(UUID commentId, VoteRequest request);

    List<CommentResponse> getCommentsByPostId(UUID postId);
}

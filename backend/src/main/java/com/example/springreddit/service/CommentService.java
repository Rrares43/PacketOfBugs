package com.example.springreddit.service;

import com.example.springreddit.dto.CommentResponse;
import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;

import java.util.UUID;

public interface CommentService {

    CommentResponse getComment(UUID commentId);

    CommentResponse createComment(UUID postId, CreateCommentRequest request);

    CommentResponse updateComment(UUID commentId, UpdateCommentRequest request);

    void deleteComment(UUID commentId);

    CommentResponse vote(UUID commentId, VoteRequest request);
}

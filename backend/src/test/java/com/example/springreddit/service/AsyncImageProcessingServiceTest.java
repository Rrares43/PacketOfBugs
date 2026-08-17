package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
import com.example.springreddit.model.ImageStatus;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncImageProcessingServiceTest {

    @InjectMocks
    private AsyncImageProcessingService asyncImageProcessingService;
    @Mock
    private ImageOptimizationService imageOptimizationService;
    @Mock
    private ImageUploadService imageUploadService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PlatformTransactionManager transactionManager;

    @Test
    void processPostImage_updatesPostWithS3UrlAndSavingsBadge() {
        UUID postId = UUID.randomUUID();
        byte[] originalBytes = new byte[300 * 1024];
        byte[] optimizedBytes = new byte[80 * 1024];
        OptimizedImageResult optimizedImage = new OptimizedImageResult(optimizedBytes, originalBytes.length, "image/jpeg");
        Post post = new Post();
        post.setId(postId);
        post.setContent("Original content");

        when(imageOptimizationService.optimize(originalBytes, "image/jpeg")).thenReturn(optimizedImage);
        when(imageUploadService.upload(any(), eq((long) optimizedBytes.length), eq("image/jpeg"), eq(".jpg"), eq(3)))
                .thenReturn("https://bucket.s3.eu-central-1.amazonaws.com/images/photo.jpg");
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        asyncImageProcessingService.processPostImage(postId, originalBytes, "photo.png", "image/jpeg", 3);

        assertEquals("https://bucket.s3.eu-central-1.amazonaws.com/images/photo.jpg", post.getImageUrl());
        assertEquals(ImageStatus.COMPLETED, post.getImageStatus());
        assertTrue(post.getContent().contains("Original content"));
        assertTrue(post.getContent().contains("storage saved"));
        verify(postRepository).save(post);
    }

    @Test
    void processPostImage_marksPostAsFailedWhenCompressionOrUploadFails() {
        UUID postId = UUID.randomUUID();
        byte[] imageBytes = new byte[]{1, 2, 3};
        Post post = new Post();
        post.setId(postId);
        post.setImageStatus(ImageStatus.PENDING);

        when(imageOptimizationService.optimize(imageBytes, "image/jpeg"))
                .thenThrow(new RuntimeException("compression failed"));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        assertDoesNotThrow(() ->
                asyncImageProcessingService.processPostImage(postId, imageBytes, "photo.jpg", "image/jpeg", null));

        assertEquals(ImageStatus.FAILED, post.getImageStatus());
        verifyNoInteractions(imageUploadService);
        verify(postRepository).save(post);
    }

    @Test
    void processPostImage_skipsUpdateWhenPostWasDeleted() {
        UUID postId = UUID.randomUUID();
        byte[] originalBytes = new byte[]{1, 2, 3};
        OptimizedImageResult optimizedImage = new OptimizedImageResult(originalBytes, originalBytes.length, "image/jpeg");
        Post post = new Post();
        post.setId(postId);
        post.softDelete();

        when(imageOptimizationService.optimize(originalBytes, "image/jpeg")).thenReturn(optimizedImage);
        when(imageUploadService.upload(any(), eq((long) originalBytes.length), eq("image/jpeg"), eq(".jpg"), isNull()))
                .thenReturn("https://bucket.s3.eu-central-1.amazonaws.com/images/photo.jpg");
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        asyncImageProcessingService.processPostImage(postId, originalBytes, "photo.jpg", "image/jpeg", null);

        verify(postRepository, never()).save(any());
    }
}

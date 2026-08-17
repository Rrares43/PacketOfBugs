package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.ImageStatus;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Background worker for image optimization, S3 upload, and post metadata updates.
 * Invoked only after the createPost transaction has committed.
 */
@Service
@RequiredArgsConstructor
public class AsyncImageProcessingService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final ImageOptimizationService imageOptimizationService;
    private final ImageUploadService imageUploadService;
    private final PostRepository postRepository;
    private final PlatformTransactionManager transactionManager;

    @Async("imageThreadPool")
    public void processPostImage(UUID postId, byte[] imageBytes, String originalFilename,
                                 String contentType, Integer filter) {
        try {
            if (postId == null || imageBytes == null || imageBytes.length == 0) {
                LOGGER.warn("Skipping async image processing for post {}: empty payload", postId);
                markImageProcessingFailed(postId);
                return;
            }

            LOGGER.info("Starting async image processing for post {}", postId);

            OptimizedImageResult optimizedImage = imageOptimizationService.optimize(imageBytes, contentType);
            String extension = "image/jpeg".equals(optimizedImage.getContentType())
                    ? ".jpg"
                    : extensionFromOriginalFilename(originalFilename);
            String imageUrl = imageUploadService.upload(
                    optimizedImage.getInputStream(),
                    optimizedImage.getOptimizedSizeBytes(),
                    optimizedImage.getContentType(),
                    extension,
                    filter);

            persistImageResult(postId, imageUrl, optimizedImage);
            LOGGER.info("Async image processing completed for post {}", postId);
        } catch (Exception exception) {
            LOGGER.error("Async image processing failed for post {}: {}", postId, exception.getMessage(), exception);
            markImageProcessingFailed(postId);
        }
    }

    private void persistImageResult(UUID postId, String imageUrl, OptimizedImageResult optimizedImage) {
        runInTransaction(postId, post -> {
            post.setImageUrl(imageUrl);
            post.setImageStatus(ImageStatus.COMPLETED);
            post.setContent(appendImageOptimizationBadge(post.getContent(), optimizedImage));
            post.setUpdatedAt(Instant.now());
            postRepository.save(post);
        });
    }

    private void markImageProcessingFailed(UUID postId) {
        if (postId == null) {
            return;
        }
        try {
            runInTransaction(postId, post -> {
                post.setImageStatus(ImageStatus.FAILED);
                post.setUpdatedAt(Instant.now());
                postRepository.save(post);
            });
        } catch (Exception exception) {
            LOGGER.error("Failed to persist FAILED image status for post {}: {}",
                    postId, exception.getMessage(), exception);
        }
    }

    private void runInTransaction(UUID postId, Consumer<Post> updater) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                LOGGER.warn("Async image processing skipped: post {} no longer exists", postId);
                return;
            }
            if (post.isDeleted()) {
                LOGGER.warn("Async image processing skipped: post {} was deleted", postId);
                return;
            }
            updater.accept(post);
        });
    }

    private String appendImageOptimizationBadge(String content, OptimizedImageResult optimizedImage) {
        if (!optimizedImage.isOptimized() || optimizedImage.getSavedPercentage() <= 0D) {
            return content;
        }

        long savedPercentage = Math.max(1L, Math.round(optimizedImage.getSavedPercentage()));
        String badge = String.format(
                Locale.US,
                "\u26A1 *Image optimized: %s \u2794 %.1f KB (-%d%% storage saved)*",
                formatOriginalImageSize(optimizedImage.getOriginalSizeBytes()),
                optimizedImage.getOptimizedSizeBytes() / 1024D,
                savedPercentage);
        return (content == null ? "" : content) + "\n\n" + badge;
    }

    private String formatOriginalImageSize(long sizeBytes) {
        if (sizeBytes >= 1024L * 1024L) {
            return String.format(Locale.US, "%.1f MB", sizeBytes / (1024D * 1024D));
        }
        return String.format(Locale.US, "%.1f KB", sizeBytes / 1024D);
    }

    private String extensionFromOriginalFilename(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}

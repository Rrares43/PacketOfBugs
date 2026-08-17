package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
import com.example.springreddit.exception.ImageSizeExceededException;
import com.example.springreddit.logging.CustomLogger;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Optimizes uploaded raster images entirely in RAM; no temporary files are created. */
@Service
public class ImageOptimizationService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private static final long SKIP_OPTIMIZATION_BELOW_BYTES = 150L * 1024L;
    private static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final long TARGET_SIZE_BYTES = 200L * 1024L;
    private static final int[] MAX_DIMENSIONS = {1200, 1000, 800, 600, 400, 300};
    private static final double[] JPEG_QUALITIES = {0.80D, 0.75D};

    public OptimizedImageResult optimize(MultipartFile file) {
        validateUpload(file);

        if (file.getSize() < SKIP_OPTIMIZATION_BELOW_BYTES || !isSupportedForOptimization(file)) {
            return original(file);
        }

        try {
            byte[] originalBytes = file.getBytes();
            if (originalBytes.length > MAX_UPLOAD_SIZE_BYTES) {
                throw new ImageSizeExceededException();
            }

            BufferedImage source = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (source == null) {
                return original(originalBytes, file.getContentType());
            }

            byte[] bestCandidate = null;
            for (int maxDimension : MAX_DIMENSIONS) {
                for (double quality : JPEG_QUALITIES) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    Thumbnails.of(source)
                            .size(maxDimension, maxDimension)
                            .outputFormat("jpg")
                            .outputQuality(quality)
                            .toOutputStream(output);

                    byte[] candidate = output.toByteArray();
                    if (bestCandidate == null || candidate.length < bestCandidate.length) {
                        bestCandidate = candidate;
                    }
                    if (candidate.length <= TARGET_SIZE_BYTES) {
                        return optimized(originalBytes.length, candidate);
                    }
                }
            }

            if (bestCandidate == null || bestCandidate.length >= originalBytes.length) {
                return original(originalBytes, file.getContentType());
            }

            LOGGER.warn("Image could not reach the {} KB target; using the smallest valid result of {} bytes",
                    TARGET_SIZE_BYTES / 1024, bestCandidate.length);
            return optimized(originalBytes.length, bestCandidate);
        } catch (ImageSizeExceededException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.warn("Image optimization skipped; raw file will be stored. Reason: {}", exception.getMessage());
            return original(file);
        }
    }

    private OptimizedImageResult optimized(long originalSize, byte[] bytes) {
        return new OptimizedImageResult(bytes, originalSize, "image/jpeg");
    }

    private OptimizedImageResult original(MultipartFile file) {
        try {
            return original(file.getBytes(), file.getContentType());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Image file could not be read.", exception);
        }
    }

    private OptimizedImageResult original(byte[] bytes, String contentType) {
        String safeContentType = contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
        return new OptimizedImageResult(bytes, bytes.length, safeContentType);
    }

    private boolean isSupportedForOptimization(MultipartFile file) {
        String contentType = file.getContentType();
        return "image/jpeg".equalsIgnoreCase(contentType) || "image/png".equalsIgnoreCase(contentType);
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }
        if (file.getSize() > MAX_UPLOAD_SIZE_BYTES) {
            throw new ImageSizeExceededException();
        }
    }
}

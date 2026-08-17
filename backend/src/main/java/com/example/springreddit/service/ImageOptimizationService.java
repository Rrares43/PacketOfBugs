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
    private static final int PRE_FILTER_MAX_DIMENSION = 1200;
    private static final double PRE_FILTER_QUALITY = 0.80D;
    private static final int[] MAX_DIMENSIONS = {1200, 1000, 800, 600, 400, 300};
    private static final double[] JPEG_QUALITIES = {0.80D, 0.75D};

    public OptimizedImageResult optimize(MultipartFile file) {
        validateUpload(file);
        try {
            return optimize(file.getBytes(), file.getContentType());
        } catch (ImageSizeExceededException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Image file could not be read.", exception);
        }
    }

    public OptimizedImageResult optimize(byte[] originalBytes, String contentType) {
        return optimize(originalBytes, contentType, originalBytes == null ? 0L : originalBytes.length);
    }

    /**
     * Re-encodes the source to a bounded JPEG before any expensive external filter runs.
     * The result never touches the filesystem and is safe to send directly to another service.
     */
    public byte[] downscaleForFiltering(byte[] originalBytes, String contentType) {
        validateBytes(originalBytes);
        if (!isSupportedForOptimization(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG images are supported.");
        }

        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (source == null) {
                throw new IllegalArgumentException("Image file could not be decoded.");
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thumbnails.of(source)
                    .size(PRE_FILTER_MAX_DIMENSION, PRE_FILTER_MAX_DIMENSION)
                    .outputFormat("jpg")
                    .outputQuality(PRE_FILTER_QUALITY)
                    .toOutputStream(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Image file could not be resized.", exception);
        }
    }

    /** Performs final storage compression while retaining the raw-upload size for the badge. */
    public OptimizedImageResult optimize(byte[] sourceBytes, String contentType, long originalSizeBytes) {
        validateBytes(sourceBytes);

        if (sourceBytes.length < SKIP_OPTIMIZATION_BELOW_BYTES || !isSupportedForOptimization(contentType)) {
            return new OptimizedImageResult(sourceBytes, originalSizeBytes, contentType);
        }

        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourceBytes));
            if (source == null) {
                return new OptimizedImageResult(sourceBytes, originalSizeBytes, contentType);
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
                        return optimized(originalSizeBytes, candidate);
                    }
                }
            }

            if (bestCandidate == null || bestCandidate.length >= sourceBytes.length) {
                return new OptimizedImageResult(sourceBytes, originalSizeBytes, contentType);
            }

            LOGGER.warn("Image could not reach the {} KB target; using the smallest valid result of {} bytes",
                    TARGET_SIZE_BYTES / 1024, bestCandidate.length);
            return optimized(originalSizeBytes, bestCandidate);
        } catch (ImageSizeExceededException exception) {
            throw exception;
        } catch (Exception exception) {
            LOGGER.warn("Image optimization skipped; raw file will be stored. Reason: {}", exception.getMessage());
            return new OptimizedImageResult(sourceBytes, originalSizeBytes, contentType);
        }
    }

    private OptimizedImageResult optimized(long originalSize, byte[] bytes) {
        return new OptimizedImageResult(bytes, originalSize, "image/jpeg");
    }

    private OptimizedImageResult original(byte[] bytes, String contentType) {
        String safeContentType = contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
        return new OptimizedImageResult(bytes, bytes.length, safeContentType);
    }

    private boolean isSupportedForOptimization(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType) || "image/png".equalsIgnoreCase(contentType);
    }

    public void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }
        if (file.getSize() > MAX_UPLOAD_SIZE_BYTES) {
            throw new ImageSizeExceededException();
        }
    }

    private void validateBytes(byte[] originalBytes) {
        if (originalBytes == null || originalBytes.length == 0) {
            throw new IllegalArgumentException("Image file is empty.");
        }
        if (originalBytes.length > MAX_UPLOAD_SIZE_BYTES) {
            throw new ImageSizeExceededException();
        }
    }
}

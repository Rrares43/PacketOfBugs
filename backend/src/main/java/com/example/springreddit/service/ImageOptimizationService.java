package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
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
    private static final long TARGET_SIZE_BYTES = 200L * 1024L;
    private static final int[] MAX_DIMENSIONS = {1200, 1000, 800, 600, 400, 300};
    private static final double[] JPEG_QUALITIES = {0.80D, 0.75D};

    public OptimizedImageResult optimize(MultipartFile file) {
        validateImageUpload(file);

        try {
            byte[] originalBytes = file.getBytes();
            if (originalBytes.length < SKIP_OPTIMIZATION_BELOW_BYTES) {
                return new OptimizedImageResult(originalBytes, originalBytes.length, file.getContentType());
            }

            BufferedImage source = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (source == null) {
                throw new IllegalArgumentException("Unsupported or invalid image file.");
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

            LOGGER.warn("Image could not reach the {} KB target; using the smallest valid result of {} bytes",
                    TARGET_SIZE_BYTES / 1024, bestCandidate.length);
            return optimized(originalBytes.length, bestCandidate);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Image could not be optimized.", exception);
        }
    }

    private OptimizedImageResult optimized(long originalSize, byte[] bytes) {
        return new OptimizedImageResult(bytes, originalSize, "image/jpeg");
    }

    private void validateImageUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty.");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }
    }
}

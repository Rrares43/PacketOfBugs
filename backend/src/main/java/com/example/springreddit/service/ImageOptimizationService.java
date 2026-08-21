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
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Optimizes uploaded raster images entirely in RAM; no temporary files are created. */
@Service
public class ImageOptimizationService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private static final long SKIP_OPTIMIZATION_BELOW_BYTES = 150L * 1024L;
    private static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final long TARGET_SIZE_BYTES = 200L * 1024L;
    private static final int PRE_FILTER_MAX_DIMENSION = 1000;
    private static final double PRE_FILTER_QUALITY = 0.75D;
    private static final int MAX_PRE_FILTER_OUTPUT_BYTES = 2 * 1024 * 1024;
    private static final int[] MAX_DIMENSIONS = {1000, 800, 600, 400, 300};
    private static final double[] JPEG_QUALITIES = {0.80D, 0.75D};

    public OptimizedImageResult optimize(MultipartFile file) {
        validateUpload(file);
        byte[] resizedBytes = downscaleForFiltering(file);
        return optimize(resizedBytes, "image/jpeg", file.getSize());
    }

    public OptimizedImageResult optimize(byte[] originalBytes, String contentType) {
        return optimize(originalBytes, contentType, originalBytes == null ? 0L : originalBytes.length);
    }

    /**
     * Re-encodes the source to a bounded JPEG before any expensive external filter runs.
     * The result never touches the filesystem and is safe to send directly to another service.
     */
    public byte[] downscaleForFiltering(MultipartFile file) {
        validateUpload(file);
        try (InputStream input = file.getInputStream()) {
            return downscaleForFiltering(input, file.getContentType());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Image file could not be read.", exception);
        }
    }

    /**
     * Decodes from the upload stream with ImageIO subsampling, then uses Thumbnailator to
     * encode a 1000x1000-at-most JPEG. No raw upload byte array is ever created.
     */
    public byte[] downscaleForFiltering(InputStream input, String contentType) {
        if (input == null) {
            throw new IllegalArgumentException("Image stream is empty.");
        }
        if (!isSupportedForOptimization(contentType)) {
            throw new IllegalArgumentException("Only JPEG and PNG images are supported.");
        }

        BufferedImage source = null;
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(input);
             ByteArrayOutputStream output = new BoundedByteArrayOutputStream(MAX_PRE_FILTER_OUTPUT_BYTES)) {
            source = readSampled(imageInput);
            Thumbnails.of(source)
                    .size(PRE_FILTER_MAX_DIMENSION, PRE_FILTER_MAX_DIMENSION)
                    .outputFormat("jpg")
                    .outputQuality(PRE_FILTER_QUALITY)
                    .toOutputStream(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Image file could not be resized.", exception);
        } finally {
            flush(source);
        }
    }

    private BufferedImage readSampled(ImageInputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Image file could not be decoded.");
        }
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) {
            throw new IllegalArgumentException("Image file could not be decoded.");
        }

        ImageReader reader = readers.next();
        try {
            reader.setInput(input, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            int sampling = Math.max(1, (int) Math.ceil(
                    Math.max(width, height) / (double) PRE_FILTER_MAX_DIMENSION));
            ImageReadParam parameters = reader.getDefaultReadParam();
            parameters.setSourceSubsampling(sampling, sampling, 0, 0);
            BufferedImage source = reader.read(0, parameters);
            if (source == null) {
                throw new IllegalArgumentException("Image file could not be decoded.");
            }
            return source;
        } finally {
            reader.dispose();
        }
    }

    /** Performs final storage compression while retaining the raw-upload size for the badge. */
    public OptimizedImageResult optimize(byte[] sourceBytes, String contentType, long originalSizeBytes) {
        validateBytes(sourceBytes);

        if (sourceBytes.length < SKIP_OPTIMIZATION_BELOW_BYTES || !isSupportedForOptimization(contentType)) {
            return new OptimizedImageResult(sourceBytes, originalSizeBytes, contentType);
        }

        BufferedImage source = null;
        try (ByteArrayInputStream input = new ByteArrayInputStream(sourceBytes)) {
            source = ImageIO.read(input);
            if (source == null) {
                return new OptimizedImageResult(sourceBytes, originalSizeBytes, contentType);
            }

            byte[] bestCandidate = null;
            for (int maxDimension : MAX_DIMENSIONS) {
                for (double quality : JPEG_QUALITIES) {
                    byte[] candidate;
                    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        Thumbnails.of(source)
                                .size(maxDimension, maxDimension)
                                .outputFormat("jpg")
                                .outputQuality(quality)
                                .toOutputStream(output);
                        candidate = output.toByteArray();
                    }
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
        } finally {
            flush(source);
        }
    }

    /** Releases the native image buffers held by AWT as soon as processing finishes. */
    private static void flush(BufferedImage image) {
        if (image != null) {
            image.flush();
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

    /** Prevents a malformed image from expanding the in-memory JPEG output without limit. */
    private static final class BoundedByteArrayOutputStream extends ByteArrayOutputStream {
        private final int maximumBytes;

        private BoundedByteArrayOutputStream(int maximumBytes) {
            super(Math.min(32 * 1024, maximumBytes));
            this.maximumBytes = maximumBytes;
        }

        @Override
        public synchronized void write(int value) {
            ensureCapacity(1);
            super.write(value);
        }

        @Override
        public synchronized void write(byte[] bytes, int offset, int length) {
            ensureCapacity(length);
            super.write(bytes, offset, length);
        }

        private void ensureCapacity(int additionalBytes) {
            if (additionalBytes < 0 || count > maximumBytes - additionalBytes) {
                throw new ImageSizeExceededException();
            }
        }
    }
}

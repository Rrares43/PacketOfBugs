package com.example.springreddit.dto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Immutable in-memory representation of an image ready for storage.
 * A fresh stream is returned for every call so callers never reuse a consumed stream.
 */
public final class OptimizedImageResult {

    private final byte[] bytes;
    private final long originalSizeBytes;
    private final long optimizedSizeBytes;
    private final String contentType;

    public OptimizedImageResult(byte[] bytes, long originalSizeBytes, String contentType) {
        this.bytes = bytes;
        this.originalSizeBytes = originalSizeBytes;
        this.optimizedSizeBytes = bytes.length;
        this.contentType = contentType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public long getOriginalSizeBytes() {
        return originalSizeBytes;
    }

    public long getOptimizedSizeBytes() {
        return optimizedSizeBytes;
    }

    public double getSavedPercentage() {
        if (originalSizeBytes == 0) {
            return 0D;
        }
        return Math.max(0D, (1D - ((double) optimizedSizeBytes / originalSizeBytes)) * 100D);
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isOptimized() {
        return optimizedSizeBytes < originalSizeBytes;
    }
}

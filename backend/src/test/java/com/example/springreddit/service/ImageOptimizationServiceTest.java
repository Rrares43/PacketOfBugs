package com.example.springreddit.service;

import com.example.springreddit.dto.OptimizedImageResult;
import com.example.springreddit.exception.ImageSizeExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageOptimizationServiceTest {

    private final ImageOptimizationService service = new ImageOptimizationService();

    @Test
    void rejectsUndecodableSmallFilesWithoutRetainingRawBytes() {
        byte[] bytes = new byte[1024];
        MockMultipartFile file = new MockMultipartFile("image", "small.png", "image/png", bytes);

        assertThrows(IllegalArgumentException.class, () -> service.optimize(file));
    }

    @Test
    void rejectsUndecodableLargeJpegWithoutRetainingRawBytes() {
        byte[] bytes = new byte[150 * 1024];
        MockMultipartFile file = new MockMultipartFile("image", "broken.jpg", "image/jpeg", bytes);

        assertThrows(IllegalArgumentException.class, () -> service.optimize(file));
    }

    @Test
    void rejectsFilesLargerThanFiveMegabytesBeforeReadingThem() {
        byte[] bytes = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile("image", "large.jpg", "image/jpeg", bytes);

        assertThrows(ImageSizeExceededException.class, () -> service.optimize(file));
    }

    @Test
    void optimizeBytesKeepsSmallPayloadsUntouched() {
        byte[] bytes = new byte[1024];

        OptimizedImageResult result = service.optimize(bytes, "image/png");

        assertEquals(bytes.length, result.getOriginalSizeBytes());
        assertEquals(bytes.length, result.getOptimizedSizeBytes());
        assertFalse(result.isOptimized());
    }
}

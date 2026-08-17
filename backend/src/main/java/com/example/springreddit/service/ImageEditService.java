package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Filter;
import com.example.springreddit.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for validating image filters and communicating with an external
 * microservice to apply specific visual filters on uploaded images.
 */
@Service
@RequiredArgsConstructor
public class ImageEditService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private final RestClient restClient = RestClient.create();

    private final FilterRepository filterRepository;

    @Value("${filter.api.url}")
    private String url;

    /**
     * Validates whether a given filter ID exists in the database.
     *
     * @param filterId the identifier of the filter to validate
     * @return the same filter ID if it exists, or null if the ID is null or not found
     */
    public Integer getValidFilterId(Integer filterId) {
        if (filterId == null) {
            return null;
        }

        if (filterRepository.existsById(filterId.longValue())) {
            return filterId;
        }

        return null;
    }

    /** Applies a filter to in-memory image bytes and returns the filtered bytes. */
    @Transactional
    public byte[] applyFilter(byte[] imageBytes, Integer filterId) {
        Filter filter = filterRepository.findById(filterId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Filter with id = " + filterId + " not found"));
        if ("none".equalsIgnoreCase(filter.getName())) {
            return imageBytes;
        }

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("image", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        }).contentType(MediaType.IMAGE_JPEG);
        body.part("filter", filter.getName());

        try {
            byte[] filteredBytes = restClient.post()
                    .uri(url)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(byte[].class);
            if (filteredBytes == null || filteredBytes.length == 0) {
                throw new IllegalStateException("Image editing service returned an empty image.");
            }
            filterRepository.incrementUsageCount(filterId.longValue());
            return filteredBytes;
        } catch (RestClientException exception) {
            LOGGER.error("Failed to communicate with image editing service for filterId {}: {}", filterId, exception.getMessage(), exception);
            throw new IllegalArgumentException("Image filtering failed.", exception);
        }
    }

    /**
     * Sends a request to the external image processing API to apply a filter on an image
     * located at a pre-signed download URL and upload the processed result to a pre-signed upload URL.
     *
     * @param downloadUrl the pre-signed S3 URL to download the original image
     * @param uploadUrl   the pre-signed S3 URL to upload the edited image
     * @param filterId    the identifier of the filter to be applied
     */
    @Transactional
    public void edit(String downloadUrl, String uploadUrl, Integer filterId) {

        LOGGER.info("Attempting to edit image with filterId: {}", filterId);

        Filter filter = filterRepository.findById(filterId.longValue())
                .orElseThrow(() -> {
                    LOGGER.warn("Image edit failed: Filter with id = {} not found", filterId);
                    return new IllegalArgumentException("Filter with id = " + filterId + " not found");
                });

        // don't send the request to edit service
        if ("none".equalsIgnoreCase(filter.getName())) {
            return;
        }

        Map<String, String> payload = Map.of(
                "downloadUrl", downloadUrl,
                "uploadUrl", uploadUrl,
                "filter", filter.getName()
        );

        try {
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            filterRepository.incrementUsageCount(Long.valueOf(filterId));
            LOGGER.info("Image edit request successfully sent for filter: {}", filter.getName());
        } catch (RestClientException e) {
            LOGGER.error("Failed to communicate with image editing service for filterId {}: {}", filterId, e.getMessage(), e);
            throw new RuntimeException("Image editing service communication failed", e);
        }
    }
}


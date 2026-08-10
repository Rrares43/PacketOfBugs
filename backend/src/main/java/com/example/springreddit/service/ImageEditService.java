package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Filter;
import com.example.springreddit.repository.FilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageEditService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private final RestClient restClient = RestClient.create();

    private final FilterRepository filterRepository;

    @Value("${filter.api.url}")
    private String url;

    public Integer getValidFilterId(Integer filterId) {
        if (filterId == null) {
            return null;
        }

        if (filterRepository.existsById(filterId.longValue())) {
            return filterId;
        }

        return null;
    }

    public void edit(String downloadUrl, String uploadUrl, Integer filterId) throws IOException {

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

            LOGGER.info("Image edit request successfully sent for filter: {}", filter.getName());
        } catch (RestClientException e) {
            LOGGER.error("Failed to communicate with image editing service for filterId {}: {}", filterId, e.getMessage(), e);
            throw new RuntimeException("Image editing service communication failed", e);
        }
    }
}


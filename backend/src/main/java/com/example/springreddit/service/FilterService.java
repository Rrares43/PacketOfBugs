package com.example.springreddit.service;

import com.example.springreddit.dto.FilterDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Filter;
import com.example.springreddit.repository.FilterRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for managing image filters.
 * Handles operations such as retrieving filters, calculating their dynamic popularity,
 * and executing scheduled jobs to reset usage statistics.
 */
@Service
public class FilterService {

    private static final String HOT_EMOJI = " 🔥";
    private static final double HOT_THRESHOLD_MULTIPLIER = 1.4;

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final FilterRepository filterRepository;
    public FilterService(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    /**
     * Retrieves all available filters from the database and maps them to DTOs.
     * Dynamically calculates the average usage of all filters and appends a popularity
     * indicator (emoji) to the label of filters that exceed the calculated threshold.
     *
     * @return a list of {@link FilterDto} containing the filter details and formatted labels
     */
    public List<FilterDto> getAllFilters() {

        LOGGER.info("Fetching all filters from the database...");
        List<Filter> filters = filterRepository.findAllByOrderByIdAsc();

        if (filters.isEmpty()) {
            LOGGER.info("No filters found in the database. Returning an empty list.");
            return List.of();
        }

        double average = filters.stream()
                .mapToInt(Filter::getUsageCount)
                .average()
                .orElse(0.0);

        double hotThreshold = average * HOT_THRESHOLD_MULTIPLIER;

        LOGGER.info("Filter usage statistics calculated - Average uses: {}, Hot Threshold: {}", average, hotThreshold);

        return filters.stream()
                .map(filter -> mapToFilterDtoWithPopularity(filter, hotThreshold))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map a {@link Filter} entity to a {@link FilterDto}.
     * Appends a 'hot' emoji to the label if the filter's usage count meets or exceeds the given threshold.
     *
     * @param filter       the filter entity to be mapped
     * @param hotThreshold the calculated threshold for determining if a filter is popular
     * @return a newly created {@link FilterDto}
     */
    private FilterDto mapToFilterDtoWithPopularity(Filter filter, double hotThreshold) {
        int count = filter.getUsageCount();
        String labelWithPopularity = filter.getLabel();

        if (count > 0 && count >= hotThreshold) {
            labelWithPopularity += HOT_EMOJI;
        }

        return new FilterDto(filter.getId(), filter.getName(), labelWithPopularity);
    }

    /**
     * Scheduled task that runs daily at midnight (00:00 server time) to reset
     * the usage count of all filters to zero.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetFilterPopularity() {
        LOGGER.info("Running scheduled job: Daily filter popularity reset...");
        filterRepository.resetAllUsageCounts();
        LOGGER.info("Filter popularity has been successfully reset to 0.");
    }
}

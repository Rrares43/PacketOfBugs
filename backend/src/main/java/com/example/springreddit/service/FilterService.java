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


@Service
public class FilterService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final FilterRepository filterRepository;
    public FilterService(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    public List<FilterDto> getAllFilters() {

        List<Filter> filters = filterRepository.findAllByOrderByIdAsc();

        int maxUses = filters.stream()
                .mapToInt(Filter::getUsageCount)
                .max()
                .orElse(0);

        return filters.stream()
                .map(filter -> {
                    int count = filter.getUsageCount();
                    String labelWithPopularity = filter.getLabel();

                    if (count > 0 && count == maxUses) {
                        labelWithPopularity += " 🔥";
                    }

                    return new FilterDto(filter.getId(), filter.getName(), labelWithPopularity);
                })
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetFilterPopularity() {
        LOGGER.info("Running scheduled job: Daily filter popularity reset...");
        filterRepository.resetAllUsageCounts();
        LOGGER.info("Filter popularity has been successfully reset to 0.");
    }
}

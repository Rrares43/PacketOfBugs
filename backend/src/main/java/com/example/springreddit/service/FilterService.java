package com.example.springreddit.service;

import com.example.springreddit.dto.FilterDto;
import com.example.springreddit.repository.FilterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class FilterService {
    private final FilterRepository filterRepository;
    public FilterService(FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    public List<FilterDto> getAllFilters() {
        return filterRepository.findAllByOrderByIdAsc().stream()
                .map(filter -> new FilterDto(filter.getId(), filter.getName(), filter.getLabel()))
                .collect(Collectors.toList());
    }
}

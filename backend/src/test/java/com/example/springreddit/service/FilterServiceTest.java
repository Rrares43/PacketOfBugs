package com.example.springreddit.service;

import com.example.springreddit.dto.FilterDto;
import com.example.springreddit.model.Filter;
import com.example.springreddit.repository.FilterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilterServiceTest {
    @InjectMocks
    private FilterService filterService;
    @Mock
    private FilterRepository filterRepository;

    @Test
    public void testGetAllFilters() {
        Filter mockFilter = new Filter(1L, "test_filter", "Test filter");
        List<Filter> mockList = new ArrayList<>();
        mockList.add(mockFilter);

        FilterDto mockDto = new FilterDto(mockFilter.getId(), mockFilter.getName(), mockFilter.getLabel());
        List<FilterDto> mockDtoList = new ArrayList<>();
        mockDtoList.add(mockDto);


        when(filterRepository.findAllByOrderByIdAsc()).thenReturn(mockList);

        List<FilterDto> retrievedList = filterService.getAllFilters();

        assertEquals(mockDtoList, retrievedList);
    }
}

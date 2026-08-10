package com.example.springreddit.repository;

import com.example.springreddit.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    List<Filter> findAllByOrderByIdAsc();
}

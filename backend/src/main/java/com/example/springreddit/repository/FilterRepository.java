package com.example.springreddit.repository;

import com.example.springreddit.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {}

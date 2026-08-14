package com.example.springreddit.repository;

import com.example.springreddit.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    List<Filter> findAllByOrderByIdAsc();

    @Modifying
    @Query("UPDATE Filter f SET f.usageCount = f.usageCount + 1 WHERE f.id = :id")
    void incrementUsageCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Filter f SET f.usageCount = 0")
    void resetAllUsageCounts();
}

package com.cardealer.repository;

import com.cardealer.model.SearchExecutionMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchExecutionMetricRepository extends JpaRepository<SearchExecutionMetric, Long> {
}

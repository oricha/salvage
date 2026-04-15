package com.cardealer.repository;

import com.cardealer.model.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    List<ViewHistory> findBySessionIdOrderByViewedAtDesc(String sessionId);

    void deleteBySessionId(String sessionId);
}

package com.cardealer.service;

import com.cardealer.dto.MarketplaceKpiSnapshot;
import com.cardealer.model.SearchExecutionMetric;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.ContactInteractionRepository;
import com.cardealer.repository.DealerRepository;
import com.cardealer.repository.SearchExecutionMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceMetricsService {

    private final SearchExecutionMetricRepository searchExecutionMetricRepository;
    private final CarRepository carRepository;
    private final DealerRepository dealerRepository;
    private final ContactInteractionRepository contactInteractionRepository;

    @Transactional
    public void recordSearchExecution(String locale, String viewMode, long resultCount) {
        SearchExecutionMetric metric = new SearchExecutionMetric();
        metric.setLocale(locale);
        metric.setViewMode(viewMode);
        metric.setResultCount(Math.max(resultCount, 0));
        searchExecutionMetricRepository.save(metric);
        log.debug("Recorded search execution viewMode={} resultCount={}", viewMode, resultCount);
    }

    @Transactional(readOnly = true)
    public MarketplaceKpiSnapshot getMarketplaceKpis() {
        long totalVehicles = carRepository.countByActiveTrue();
        long totalDealers = dealerRepository.countByActiveTrue();
        long totalSearches = searchExecutionMetricRepository.count();
        long totalViews = carRepository.sumViewsByActiveTrue();
        long totalContactInteractions = contactInteractionRepository.count();

        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalViews > 0 && totalContactInteractions > 0) {
            conversionRate = BigDecimal.valueOf(totalContactInteractions)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalViews), 2, RoundingMode.HALF_UP);
        }

        return new MarketplaceKpiSnapshot(
            totalVehicles,
            totalDealers,
            totalSearches,
            totalViews,
            totalContactInteractions,
            conversionRate
        );
    }
}

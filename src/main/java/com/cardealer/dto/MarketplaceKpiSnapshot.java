package com.cardealer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceKpiSnapshot {

    private Long totalVehicles;
    private Long totalDealers;
    private Long totalSearches;
    private Long totalVehicleViews;
    private Long totalContactInteractions;
    private BigDecimal contactConversionRate;
}

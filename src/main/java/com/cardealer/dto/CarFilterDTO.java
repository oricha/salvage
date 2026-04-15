package com.cardealer.dto;

import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.VehicleCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarFilterDTO {
    
    private List<String> brands;
    private Integer yearFrom;
    private Integer yearTo;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String transmission;
    private String fuelType;
    private String bodyType;
    private String condition;
    private List<CarCondition> conditions;
    private List<VehicleCategory> categories;
    private Integer minMileage;
    private Integer maxMileage;
    private List<String> features;
    private String sortBy;  // price_asc, price_desc, date_desc, mileage_asc, year_desc
    private String searchText;  // For text search in brand, model, description
    private String searchQuery;
    private String locale;
}

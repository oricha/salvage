package com.cardealer.dto;

import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.TriStateOption;
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
    private String model;
    private Integer yearFrom;
    private Integer yearTo;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String color;
    private String colorCode;
    private String transmission;
    private List<String> transmissions;
    private String fuelType;
    private List<String> fuelTypes;
    private String bodyType;
    private List<String> bodyTypes;
    private TriStateOption refinedFuelType;
    private String condition;
    private List<CarCondition> conditions;
    private List<VehicleCategory> categories;
    private Integer minMileage;
    private Integer maxMileage;
    private List<String> origins;
    private Integer nearbyRadiusKm;
    private Double referenceLatitude;
    private Double referenceLongitude;
    private TriStateOption registrationAvailable;
    private Boolean awaitingVerification;
    private TriStateOption fullInstructionBooklet;
    private TriStateOption allKeysAvailable;
    private TriStateOption engineDamage;
    private TriStateOption lowerDamage;
    private TriStateOption drivable;
    private TriStateOption movable;
    private TriStateOption engineRuns;
    private TriStateOption airbagsIntact;
    private List<String> features;
    private String sortBy;  // price_asc, price_desc, date_desc, mileage_asc, year_desc
    private String searchText;  // For text search in brand, model, description
    private String searchQuery;
    private String locale;
}

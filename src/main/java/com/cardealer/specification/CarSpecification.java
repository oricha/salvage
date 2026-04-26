package com.cardealer.specification;

import com.cardealer.catalog.AdvancedSearchCatalog;
import com.cardealer.dto.CarFilterDTO;
import com.cardealer.model.Car;
import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.FuelType;
import com.cardealer.model.enums.TransmissionType;
import com.cardealer.model.enums.TriStateOption;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.model.enums.VehicleOrigin;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CarSpecification {

    public static Specification<Car> buildSpecification(CarFilterDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by active = true
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            // Filter by brands
            if (filters.getBrands() != null && !filters.getBrands().isEmpty()) {
                predicates.add(root.get("make").in(filters.getBrands()));
            }

            if (filters.getModel() != null
                    && !filters.getModel().isBlank()
                    && filters.getBrands() != null
                    && !filters.getBrands().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("model")),
                    filters.getModel().toLowerCase(Locale.ROOT)
                ));
            }

            // Filter by price range
            if (filters.getYearFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("year"), filters.getYearFrom()));
            }
            if (filters.getYearTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("year"), filters.getYearTo()));
            }

            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), filters.getMinPrice()));
            }
            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), filters.getMaxPrice()));
            }

            if (filters.getColor() != null && !filters.getColor().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("color")),
                    filters.getColor().toLowerCase(Locale.ROOT)
                ));
            }

            if (filters.getColorCode() != null && !filters.getColorCode().isBlank()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("colorCode")),
                    "%" + filters.getColorCode().toLowerCase(Locale.ROOT) + "%"
                ));
            }

            // Filter by transmission
            Set<TransmissionType> transmissions = resolveTransmissions(filters);
            if (!transmissions.isEmpty()) {
                predicates.add(root.get("transmission").in(transmissions));
            }

            // Filter by fuel type
            Set<FuelType> fuelTypes = resolveFuelTypes(filters);
            if (!fuelTypes.isEmpty()) {
                predicates.add(root.get("fuelType").in(fuelTypes));
            }

            // Filter by body type
            Set<BodyType> bodyTypes = resolveBodyTypes(filters);
            if (!bodyTypes.isEmpty()) {
                predicates.add(root.get("bodyType").in(bodyTypes));
            }

            // Filter by condition
            if (filters.getCondition() != null && !filters.getCondition().isEmpty()) {
                try {
                    CarCondition condition = CarCondition.valueOf(filters.getCondition().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("condition"), condition));
                } catch (IllegalArgumentException e) {
                    // Invalid condition, ignore filter
                }
            }

            if (filters.getConditions() != null && !filters.getConditions().isEmpty()) {
                predicates.add(root.get("condition").in(filters.getConditions()));
            }

            if (filters.getCategories() != null && !filters.getCategories().isEmpty()) {
                predicates.add(root.get("category").in(filters.getCategories()));
            }

            if (filters.getMinMileage() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("mileage"), filters.getMinMileage()));
            }

            if (filters.getMaxMileage() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("mileage"), filters.getMaxMileage()));
            }

            Set<VehicleOrigin> origins = resolveOrigins(filters);
            if (!origins.isEmpty()) {
                predicates.add(root.get("origin").in(origins));
            }

            if (filters.getNearbyRadiusKm() != null
                    && filters.getNearbyRadiusKm() > 0
                    && filters.getReferenceLatitude() != null
                    && filters.getReferenceLongitude() != null) {
                double latitude = filters.getReferenceLatitude();
                double longitude = filters.getReferenceLongitude();
                double latDelta = filters.getNearbyRadiusKm() / 111.0d;
                double lngDelta = filters.getNearbyRadiusKm() / Math.max(1.0d, 111.0d * Math.cos(Math.toRadians(latitude)));
                var dealerJoin = root.join("dealer", JoinType.LEFT);
                predicates.add(criteriaBuilder.isNotNull(dealerJoin.get("latitude")));
                predicates.add(criteriaBuilder.isNotNull(dealerJoin.get("longitude")));
                predicates.add(criteriaBuilder.between(dealerJoin.get("latitude"), latitude - latDelta, latitude + latDelta));
                predicates.add(criteriaBuilder.between(dealerJoin.get("longitude"), longitude - lngDelta, longitude + lngDelta));
            }

            applyTriStatePredicate(predicates, criteriaBuilder, root.get("refinedFuelType"), filters.getRefinedFuelType());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("registrationAvailable"), filters.getRegistrationAvailable());
            if (Boolean.TRUE.equals(filters.getAwaitingVerification())) {
                predicates.add(criteriaBuilder.isTrue(root.get("awaitingVerification")));
            }
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("fullInstructionBooklet"), filters.getFullInstructionBooklet());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("allKeysAvailable"), filters.getAllKeysAvailable());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("engineDamage"), filters.getEngineDamage());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("lowerDamage"), filters.getLowerDamage());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("drivable"), filters.getDrivable());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("movable"), filters.getMovable());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("engineRuns"), filters.getEngineRuns());
            applyTriStatePredicate(predicates, criteriaBuilder, root.get("airbagsIntact"), filters.getAirbagsIntact());

            // Filter by search text (brand, model, description)
            String queryText = filters.getSearchQuery() != null && !filters.getSearchQuery().isEmpty()
                ? filters.getSearchQuery()
                : filters.getSearchText();
            if ((filters.getModel() == null || filters.getModel().isBlank())
                    && queryText != null && !queryText.isEmpty()) {
                String searchPattern = "%" + queryText.toLowerCase() + "%";
                Predicate brandMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("make")), searchPattern);
                Predicate modelMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("model")), searchPattern);
                Predicate descriptionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern);
                
                predicates.add(criteriaBuilder.or(brandMatch, modelMatch, descriptionMatch));
            }

            if (filters.getLocale() != null && !filters.getLocale().isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("locale")), filters.getLocale().toLowerCase()));
            }

            // Filter by features (car must have all selected features)
            if (filters.getFeatures() != null && !filters.getFeatures().isEmpty()) {
                for (String feature : filters.getFeatures()) {
                    predicates.add(criteriaBuilder.isMember(feature, root.get("features")));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Set<TransmissionType> resolveTransmissions(CarFilterDTO filters) {
        Set<TransmissionType> transmissions = new LinkedHashSet<>();
        if (filters.getTransmissions() != null) {
            filters.getTransmissions().forEach(value -> addTransmission(transmissions, value));
        }
        addTransmission(transmissions, filters.getTransmission());
        return transmissions;
    }

    private static void addTransmission(Set<TransmissionType> transmissions, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            transmissions.add(TransmissionType.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static Set<FuelType> resolveFuelTypes(CarFilterDTO filters) {
        Set<FuelType> fuelTypes = new LinkedHashSet<>();
        if (filters.getFuelTypes() != null) {
            filters.getFuelTypes().forEach(value -> addFuelType(fuelTypes, value));
        }
        addFuelType(fuelTypes, filters.getFuelType());
        return fuelTypes;
    }

    private static void addFuelType(Set<FuelType> fuelTypes, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            fuelTypes.add(FuelType.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static Set<BodyType> resolveBodyTypes(CarFilterDTO filters) {
        Set<BodyType> bodyTypes = new LinkedHashSet<>();
        Map<String, List<BodyType>> mappings = AdvancedSearchCatalog.bodyTypeMappings();
        if (filters.getBodyTypes() != null) {
            filters.getBodyTypes().forEach(value -> addBodyType(bodyTypes, value, mappings));
        }
        addBodyType(bodyTypes, filters.getBodyType(), mappings);
        return bodyTypes;
    }

    private static void addBodyType(Set<BodyType> bodyTypes, String value, Map<String, List<BodyType>> mappings) {
        if (value == null || value.isBlank()) {
            return;
        }
        List<BodyType> mapped = mappings.get(value.toUpperCase(Locale.ROOT));
        if (mapped != null) {
            bodyTypes.addAll(mapped);
            return;
        }
        try {
            bodyTypes.add(BodyType.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static Set<VehicleOrigin> resolveOrigins(CarFilterDTO filters) {
        Set<VehicleOrigin> origins = new LinkedHashSet<>();
        if (filters.getOrigins() == null) {
            return origins;
        }
        filters.getOrigins().forEach(value -> {
            if (value == null || value.isBlank()) {
                return;
            }
            try {
                origins.add(VehicleOrigin.valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        });
        return origins;
    }

    private static void applyTriStatePredicate(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            jakarta.persistence.criteria.Path<Boolean> path,
            TriStateOption option
    ) {
        if (option == null || option == TriStateOption.BOTH) {
            return;
        }
        if (option == TriStateOption.YES) {
            predicates.add(criteriaBuilder.isTrue(path));
            return;
        }
        predicates.add(criteriaBuilder.isFalse(path));
    }
}

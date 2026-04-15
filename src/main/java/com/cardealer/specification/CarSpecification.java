package com.cardealer.specification;

import com.cardealer.dto.CarFilterDTO;
import com.cardealer.model.Car;
import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.FuelType;
import com.cardealer.model.enums.TransmissionType;
import com.cardealer.model.enums.VehicleCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

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

            // Filter by price range
            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), filters.getMinPrice()));
            }
            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), filters.getMaxPrice()));
            }

            // Filter by transmission
            if (filters.getTransmission() != null && !filters.getTransmission().isEmpty()) {
                try {
                    TransmissionType transmission = TransmissionType.valueOf(filters.getTransmission().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("transmission"), transmission));
                } catch (IllegalArgumentException e) {
                    // Invalid transmission type, ignore filter
                }
            }

            // Filter by fuel type
            if (filters.getFuelType() != null && !filters.getFuelType().isEmpty()) {
                try {
                    FuelType fuelType = FuelType.valueOf(filters.getFuelType().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("fuelType"), fuelType));
                } catch (IllegalArgumentException e) {
                    // Invalid fuel type, ignore filter
                }
            }

            // Filter by body type
            if (filters.getBodyType() != null && !filters.getBodyType().isEmpty()) {
                try {
                    BodyType bodyType = BodyType.valueOf(filters.getBodyType().toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("bodyType"), bodyType));
                } catch (IllegalArgumentException e) {
                    // Invalid body type, ignore filter
                }
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

            // Filter by search text (brand, model, description)
            String queryText = filters.getSearchQuery() != null && !filters.getSearchQuery().isEmpty()
                ? filters.getSearchQuery()
                : filters.getSearchText();
            if (queryText != null && !queryText.isEmpty()) {
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
}

package com.cardealer.property;

import com.cardealer.dto.CarDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleListingValidationPropertiesTest {

    private final Validator validator;

    VehicleListingValidationPropertiesTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 1: Vehicle Listing Required Fields Validation")
    void vehicleListingRequiredFieldsValidation(@ForAll boolean missingBrand,
                                                @ForAll boolean missingModel,
                                                @ForAll boolean missingYear,
                                                @ForAll boolean missingPrice,
                                                @ForAll boolean missingFuelType,
                                                @ForAll boolean missingTransmission,
                                                @ForAll boolean missingCondition,
                                                @ForAll boolean missingCategory) {
        CarDTO dto = validCarDto();
        if (missingBrand) {
            dto.setBrand(" ");
        }
        if (missingModel) {
            dto.setModel(" ");
        }
        if (missingYear) {
            dto.setYear(null);
        }
        if (missingPrice) {
            dto.setPrice(null);
        }
        if (missingFuelType) {
            dto.setFuelType(" ");
        }
        if (missingTransmission) {
            dto.setTransmission(" ");
        }
        if (missingCondition) {
            dto.setCondition(" ");
        }
        if (missingCategory) {
            dto.setCategory(" ");
        }

        Set<ConstraintViolation<CarDTO>> violations = validator.validate(dto);

        if (missingBrand || missingModel || missingYear || missingPrice || missingFuelType
            || missingTransmission || missingCondition || missingCategory) {
            assertTrue(violations.stream().anyMatch(v -> List.of(
                "brand", "model", "year", "price", "fuelType", "transmission", "condition", "category"
            ).contains(v.getPropertyPath().toString())));
        } else {
            assertFalse(violations.stream().anyMatch(v -> List.of(
                "brand", "model", "year", "price", "fuelType", "transmission", "condition", "category"
            ).contains(v.getPropertyPath().toString())));
        }
    }

    private CarDTO validCarDto() {
        CarDTO dto = new CarDTO();
        dto.setBrand("Toyota");
        dto.setModel("Corolla");
        dto.setYear(2024);
        dto.setPrice(BigDecimal.valueOf(23500));
        dto.setMileage(1200);
        dto.setFuelType("GASOLINA");
        dto.setTransmission("AUTOMATICO");
        dto.setCondition("OCASION");
        dto.setCategory("PASSENGER_CAR");
        dto.setLocale("es");
        dto.setImageFiles(List.of(mockImage(), mockImage(), mockImage(), mockImage(), mockImage(),
            mockImage(), mockImage(), mockImage(), mockImage(), mockImage(),
            mockImage(), mockImage(), mockImage(), mockImage(), mockImage(),
            mockImage(), mockImage(), mockImage(), mockImage(), mockImage()));
        return dto;
    }

    private MockMultipartFile mockImage() {
        return new MockMultipartFile("image", "vehicle.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }
}

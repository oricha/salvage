package com.cardealer.property;

import com.cardealer.dto.CarDTO;
import com.cardealer.model.Car;
import com.cardealer.model.enums.VehicleCategory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehicleListingPropertiesTest {

    private final Validator validator;

    VehicleListingPropertiesTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 21: Vehicle Condition Category Requirement")
    void vehicleConditionCategoryRequirement(@ForAll("categoryCandidates") String category) {
        CarDTO dto = validCarDto();
        dto.setCategory(category);

        Set<ConstraintViolation<CarDTO>> violations = validator.validate(dto);
        boolean hasCategoryViolation = violations.stream().anyMatch(v -> "category".equals(v.getPropertyPath().toString()));

        if (category == null || category.isBlank()) {
            assertTrue(hasCategoryViolation);
        } else {
            assertFalse(hasCategoryViolation);
        }
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 2: Image Count Constraint")
    void imageCountConstraint(@ForAll @IntRange(min = 0, max = 50) int imageCount)
        throws NoSuchMethodException {
        CarDTO dto = validCarDto();
        dto.setImageFiles(List.copyOf(java.util.Collections.nCopies(imageCount, mockImage())));

        Set<ConstraintViolation<CarDTO>> dtoViolations = validator.validate(dto);
        boolean hasImageCountViolation = dtoViolations.stream().anyMatch(v -> "imageFiles".equals(v.getPropertyPath().toString()));

        Car car = new Car();
        car.setCategory(VehicleCategory.PASSENGER_CAR);
        car.setImages(java.util.Collections.nCopies(imageCount, "image.jpg"));
        Method validateMethod = Car.class.getDeclaredMethod("validateImageCount");
        validateMethod.setAccessible(true);

        if (imageCount >= 20 && imageCount <= 25) {
            assertFalse(hasImageCountViolation);
            assertDoesNotThrow(() -> invokeValidate(validateMethod, car));
        } else if (imageCount == 0) {
            assertTrue(hasImageCountViolation);
            assertDoesNotThrow(() -> invokeValidate(validateMethod, car));
        } else {
            assertTrue(hasImageCountViolation);
            assertThrows(IllegalStateException.class, () -> invokeValidate(validateMethod, car));
        }
    }

    private void invokeValidate(Method method, Car car) throws Throwable {
        try {
            method.invoke(car);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private CarDTO validCarDto() {
        CarDTO dto = new CarDTO();
        dto.setBrand("Ford");
        dto.setModel("Focus");
        dto.setYear(2023);
        dto.setPrice(BigDecimal.valueOf(15000));
        dto.setMileage(45000);
        dto.setFuelType("GASOLINA");
        dto.setTransmission("MANUAL");
        dto.setCondition("OCASION");
        dto.setCategory("PASSENGER_CAR");
        dto.setImageFiles(List.copyOf(java.util.Collections.nCopies(20, mockImage())));
        return dto;
    }

    private MockMultipartFile mockImage() {
        return new MockMultipartFile("image", "vehicle.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }

    @net.jqwik.api.Provide
    net.jqwik.api.Arbitrary<String> categoryCandidates() {
        return Arbitraries.of(null, "", " ", "PASSENGER_CAR", "DAMAGED", "MOTORCYCLE");
    }
}

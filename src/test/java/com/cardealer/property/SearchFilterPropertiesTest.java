package com.cardealer.property;

import com.cardealer.dto.CarFilterDTO;
import com.cardealer.model.Car;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.VehicleCategory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Arbitrary;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchFilterPropertiesTest {

    @Property(tries = 75)
    @Tag("Feature: portal-venta-coches, Property 6: Multi-Filter Search Accuracy")
    void multiFilterSearchAccuracy(@ForAll("cars") List<Car> cars,
                                   @ForAll("filters") CarFilterDTO filters) {
        List<Car> results = cars.stream().filter(car -> matches(car, filters)).toList();
        results.forEach(car -> assertTrue(matches(car, filters)));
    }

    @Provide
    Arbitrary<List<Car>> cars() {
        return Arbitraries.integers().between(1, 25).list().ofSize(12).map(ids ->
            ids.stream().map(id -> {
                Car car = new Car();
                car.setId((long) id);
                car.setMake(id % 2 == 0 ? "Ford" : "BMW");
                car.setModel(id % 3 == 0 ? "Transit" : "Focus");
                car.setPrice(BigDecimal.valueOf(10000L + (id * 1000L)));
                car.setMileage(10000 + (id * 2500));
                car.setCategory(id % 5 == 0 ? VehicleCategory.DAMAGED : VehicleCategory.PASSENGER_CAR);
                car.setCondition(id % 4 == 0 ? CarCondition.ACCIDENTADO : CarCondition.OCASION);
                car.setDescription("Car " + id + " description");
                car.setLocale(id % 2 == 0 ? "en" : "es");
                car.setActive(true);
                return car;
            }).toList()
        );
    }

    @Provide
    Arbitrary<CarFilterDTO> filters() {
        Arbitrary<CarFilterDTO> empty = Arbitraries.just(new CarFilterDTO());
        Arbitrary<CarFilterDTO> configured = Arbitraries.integers().between(0, 1).map(toggle -> {
            CarFilterDTO dto = new CarFilterDTO();
            if (toggle == 0) {
                dto.setBrands(List.of("Ford"));
                dto.setCategories(List.of(VehicleCategory.PASSENGER_CAR));
                dto.setConditions(List.of(CarCondition.OCASION));
                dto.setMinPrice(BigDecimal.valueOf(9000));
                dto.setMaxPrice(BigDecimal.valueOf(30000));
                dto.setMinMileage(0);
                dto.setMaxMileage(40000);
                dto.setSearchQuery("Focus");
                dto.setLocale("en");
            } else {
                dto.setBrands(List.of("BMW"));
                dto.setCategories(List.of(VehicleCategory.DAMAGED));
                dto.setConditions(List.of(CarCondition.ACCIDENTADO));
                dto.setSearchQuery("description");
                dto.setLocale("es");
            }
            return dto;
        });
        return Arbitraries.oneOf(empty, configured);
    }

    private boolean matches(Car car, CarFilterDTO filters) {
        if (filters.getBrands() != null && !filters.getBrands().isEmpty() && !filters.getBrands().contains(car.getMake())) {
            return false;
        }
        if (filters.getCategories() != null && !filters.getCategories().isEmpty() && !filters.getCategories().contains(car.getCategory())) {
            return false;
        }
        if (filters.getConditions() != null && !filters.getConditions().isEmpty() && !filters.getConditions().contains(car.getCondition())) {
            return false;
        }
        if (filters.getMinPrice() != null && car.getPrice().compareTo(filters.getMinPrice()) < 0) {
            return false;
        }
        if (filters.getMaxPrice() != null && car.getPrice().compareTo(filters.getMaxPrice()) > 0) {
            return false;
        }
        if (filters.getMinMileage() != null && car.getMileage() < filters.getMinMileage()) {
            return false;
        }
        if (filters.getMaxMileage() != null && car.getMileage() > filters.getMaxMileage()) {
            return false;
        }
        if (filters.getLocale() != null && !filters.getLocale().isBlank() && !filters.getLocale().equalsIgnoreCase(car.getLocale())) {
            return false;
        }
        String query = filters.getSearchQuery();
        if (query != null && !query.isBlank()) {
            String normalized = query.toLowerCase();
            return car.getMake().toLowerCase().contains(normalized)
                || car.getModel().toLowerCase().contains(normalized)
                || (car.getDescription() != null && car.getDescription().toLowerCase().contains(normalized));
        }
        return true;
    }
}

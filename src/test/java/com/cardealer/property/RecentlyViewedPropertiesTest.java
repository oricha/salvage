package com.cardealer.property;

import com.cardealer.model.Car;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.ViewHistoryRepository;
import com.cardealer.service.RecentlyViewedService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecentlyViewedPropertiesTest {

    @Property
    @Tag("Feature: portal-venta-coches, Property 11: Recently Viewed Tracking")
    void recentlyViewedTracking(@ForAll @Size(min = 1, max = 20) @UniqueElements List<Long> viewedCarIds) {
        CarRepository carRepository = mock(CarRepository.class);
        ViewHistoryRepository viewHistoryRepository = mock(ViewHistoryRepository.class);
        RecentlyViewedService service = new RecentlyViewedService(carRepository, viewHistoryRepository);
        MockHttpSession session = new MockHttpSession();

        List<Car> cars = viewedCarIds.stream().map(this::carWithId).toList();
        when(carRepository.findAllById(any(Iterable.class))).thenReturn(cars);
        viewedCarIds.forEach(id -> when(carRepository.findById(id)).thenReturn(Optional.of(carWithId(id))));

        viewedCarIds.forEach(id -> service.addToRecentlyViewed(id, session));

        List<Long> recentlyViewed = service.getRecentlyViewed(session, 10);
        List<Long> expected = viewedCarIds.subList(Math.max(0, viewedCarIds.size() - 10), viewedCarIds.size()).reversed();
        assertEquals(expected, recentlyViewed);
        assertEquals(Math.min(5, expected.size()), service.getRecentlyViewedCars(session, 5).size());
    }

    private Car carWithId(Long id) {
        Car car = new Car();
        car.setId(id);
        car.setMake("Make" + id);
        car.setModel("Model" + id);
        return car;
    }
}

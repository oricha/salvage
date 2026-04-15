package com.cardealer.property;

import com.cardealer.model.Car;
import com.cardealer.model.SEOMetadataEntity;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.SEOMetadataRepository;
import com.cardealer.service.SEOService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;
import org.junit.jupiter.api.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SEOPropertiesTest {

    @Property
    @Tag("Feature: portal-venta-coches, Property 15: SEO Metadata Uniqueness")
    void seoMetadataUniqueness(@ForAll @IntRange(min = 1, max = 10_000) int firstId,
                               @ForAll @IntRange(min = 10_001, max = 20_000) int secondId) {
        SEOService service = seoService();
        Car first = car((long) firstId, "Ford", "Focus");
        Car second = car((long) secondId, "Ford", "Focus RS");

        SEOMetadataEntity firstMetadata = service.generateCarMetadata(first, Locale.ENGLISH);
        SEOMetadataEntity secondMetadata = service.generateCarMetadata(second, Locale.ENGLISH);

        assertNotEquals(firstMetadata.getMetaTitle(), secondMetadata.getMetaTitle());
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 16: SEO Sitemap Completeness")
    void seoSitemapCompleteness(@ForAll @Size(min = 1, max = 10) @UniqueElements List<Long> carIds) {
        CarRepository carRepository = mock(CarRepository.class);
        SEOMetadataRepository seoRepository = mock(SEOMetadataRepository.class);
        when(seoRepository.findByPageKeyAndLocale(any(), any())).thenReturn(Optional.empty());
        when(seoRepository.save(any(SEOMetadataEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carRepository.findAllByActiveTrue()).thenReturn(carIds.stream().map(id -> car(id, "Make" + id, "Model" + id)).toList());
        SEOService service = new SEOService(seoRepository, carRepository);

        String sitemap = service.generateSitemap();

        assertTrue(sitemap.contains("https://portalcoches.com/"));
        assertTrue(sitemap.contains("https://portalcoches.com/cars"));
        carIds.forEach(id -> assertTrue(sitemap.contains("https://portalcoches.com/cars/" + id)));
    }

    private SEOService seoService() {
        CarRepository carRepository = mock(CarRepository.class);
        SEOMetadataRepository seoRepository = mock(SEOMetadataRepository.class);
        when(seoRepository.findByPageKeyAndLocale(any(), any())).thenReturn(Optional.empty());
        when(seoRepository.save(any(SEOMetadataEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carRepository.findAllByActiveTrue()).thenReturn(List.of());
        return new SEOService(seoRepository, carRepository);
    }

    private Car car(Long id, String make, String model) {
        Car car = new Car();
        car.setId(id);
        car.setMake(make);
        car.setModel(model);
        car.setYear(2023);
        car.setPrice(BigDecimal.valueOf(25000));
        car.setMileage(50000);
        car.setCondition(CarCondition.OCASION);
        car.setCategory(VehicleCategory.PASSENGER_CAR);
        car.setLocale("en");
        car.setDescription("Vehicle " + make + " " + model);
        return car;
    }
}

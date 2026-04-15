package com.cardealer.repository;

import com.cardealer.model.Car;
import com.cardealer.model.enums.VehicleCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
    
    // Find cars by make
    List<Car> findByMakeIgnoreCaseAndActiveTrue(String make);
    
    // Find cars by year
    List<Car> findByYearAndActiveTrue(Integer year);
    
    // Find cars ordered by price ascending
    List<Car> findAllByActiveTrueOrderByPriceAsc();
    
    // Find cars ordered by price descending
    List<Car> findAllByActiveTrueOrderByPriceDesc();
    
    // Find cars ordered by year descending (newest first)
    List<Car> findAllByActiveTrueOrderByYearDesc();
    
    // Find active cars with pagination
    Page<Car> findByActiveTrue(Pageable pageable);
    Page<Car> findByCategoryAndActiveTrue(VehicleCategory category, Pageable pageable);

    List<Car> findAllByActiveTrue();
    List<Car> findByLocaleIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(String locale);
    
    // Find cars by dealer ID ordered by creation date
    List<Car> findByDealerIdOrderByCreatedAtDesc(Long dealerId);
    
    // Find related cars (same brand, different ID, active)
    List<Car> findTop6ByMakeAndIdNotAndActiveTrue(String make, Long id);
    
    // Find latest active cars
    List<Car> findTop8ByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT c.make FROM Car c WHERE c.active = true ORDER BY c.make")
    List<String> findDistinctMakesByActiveTrue();
    
    // Count active listings by dealer
    Long countByDealerIdAndActiveTrue(Long dealerId);

    Long countByActiveTrue();
    
    // Sum views by dealer
    @Query("SELECT SUM(c.views) FROM Car c WHERE c.dealer.id = :dealerId")
    Long sumViewsByDealerId(@Param("dealerId") Long dealerId);
}

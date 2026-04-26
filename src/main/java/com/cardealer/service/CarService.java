package com.cardealer.service;

import com.cardealer.dto.CarDTO;
import com.cardealer.dto.CarFilterDTO;
import com.cardealer.dto.DashboardStats;
import com.cardealer.exception.ResourceNotFoundException;
import com.cardealer.exception.ImageProcessingException;
import com.cardealer.exception.UnauthorizedException;
import com.cardealer.model.Car;
import com.cardealer.model.Dealer;
import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.FuelType;
import com.cardealer.model.enums.TransmissionType;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.model.enums.VehicleOrigin;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.DealerRepository;
import com.cardealer.specification.CarSpecification;
import com.cardealer.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;
    private final DealerRepository dealerRepository;
    private final FileUploadUtil fileUploadUtil;

    /**
     * Find cars with filters and pagination
     */
    @Cacheable("vehicles")
    public Page<Car> findCarsWithFilters(CarFilterDTO filters, Pageable pageable) {
        log.info("Finding cars with filters: {}", filters);
        
        // Build specification from filters
        Specification<Car> spec = CarSpecification.buildSpecification(filters);
        
        // Apply sorting if specified
        Pageable pageableWithSort = pageable;
        if (filters.getSortBy() != null && !filters.getSortBy().isEmpty()) {
            Sort sort = getSortFromString(filters.getSortBy());
            pageableWithSort = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                sort
            );
        }
        
        return carRepository.findAll(spec, pageableWithSort);
    }

    /**
     * Get car by ID and increment views
     */
    @Transactional
    public Car getCarById(Long id) {
        log.info("Fetching car with id: {}", id);
        
        Car car = carRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + id));
        
        // Increment views
        car.setViews(car.getViews() + 1);
        carRepository.save(car);
        
        log.debug("Car found and views incremented: {}", car.getId());
        return car;
    }

    /**
     * Create a new car
     */
    @Transactional
    @CacheEvict(value = "latestCars", allEntries = true)
    public Car createCar(CarDTO carDTO, Long dealerId) throws IOException {
        log.info("Creating new car for dealer: {}", dealerId);
        
        // Validate dealer exists
        Dealer dealer = dealerRepository.findById(dealerId)
            .orElseThrow(() -> new ResourceNotFoundException("Concesionario no encontrado con id: " + dealerId));
        
        // Create car entity from DTO
        Car car = new Car();
        mapDtoToEntity(carDTO, car);
        car.setDealer(dealer);
        car.setActive(true);
        car.setViews(0);
        handleImages(carDTO, car, false);
        
        Car savedCar = carRepository.save(car);
        log.info("Car created successfully with id: {}", savedCar.getId());
        
        return savedCar;
    }

    /**
     * Update an existing car
     */
    @Transactional
    @CacheEvict(value = "latestCars", allEntries = true)
    public Car updateCar(Long id, CarDTO carDTO, Long dealerId) throws IOException {
        log.info("Updating car with id: {} for dealer: {}", id, dealerId);
        
        Car car = carRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + id));
        
        // Verify that the car belongs to the dealer
        if (!car.getDealer().getId().equals(dealerId)) {
            log.error("Unauthorized attempt to update car {} by dealer {}", id, dealerId);
            throw new UnauthorizedException("No tienes permisos para actualizar este coche");
        }
        
        // Update car fields
        mapDtoToEntity(carDTO, car);
        handleImages(carDTO, car, true);
        
        Car updatedCar = carRepository.save(car);
        log.info("Car updated successfully: {}", updatedCar.getId());
        
        return updatedCar;
    }

    /**
     * Delete a car (soft delete)
     */
    @Transactional
    @CacheEvict(value = "latestCars", allEntries = true)
    public void deleteCar(Long id, Long dealerId) {
        log.info("Deleting car with id: {} for dealer: {}", id, dealerId);
        
        Car car = carRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + id));
        
        // Verify that the car belongs to the dealer
        if (!car.getDealer().getId().equals(dealerId)) {
            log.error("Unauthorized attempt to delete car {} by dealer {}", id, dealerId);
            throw new UnauthorizedException("No tienes permisos para eliminar este coche");
        }
        
        // Soft delete - set active to false
        car.setActive(false);
        carRepository.save(car);
        
        log.info("Car soft deleted successfully: {}", id);
    }

    /**
     * Reactivate a previously deactivated car
     */
    @Transactional
    @CacheEvict(value = "latestCars", allEntries = true)
    public Car reactivateCar(Long id, Long dealerId) {
        log.info("Reactivating car with id: {} for dealer: {}", id, dealerId);

        Car car = carRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + id));

        if (!car.getDealer().getId().equals(dealerId)) {
            log.error("Unauthorized attempt to reactivate car {} by dealer {}", id, dealerId);
            throw new UnauthorizedException("No tienes permisos para reactivar este coche");
        }

        car.setActive(true);
        return carRepository.save(car);
    }

    /**
     * Get cars by dealer
     */
    public List<Car> getCarsByDealer(Long dealerId) {
        log.info("Fetching cars for dealer: {}", dealerId);
        return carRepository.findByDealerIdOrderByCreatedAtDesc(dealerId);
    }

    /**
     * Get related cars (same brand, different ID, active)
     */
    public List<Car> getRelatedCars(Long carId) {
        log.info("Fetching related cars for car: {}", carId);
        
        Car car = carRepository.findById(carId)
            .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + carId));
        
        return carRepository.findTop6ByMakeAndIdNotAndActiveTrue(car.getMake(), carId);
    }

    /**
     * Get latest active cars
     */
    @Cacheable("latestCars")
    public List<Car> getLatestCars() {
        log.info("Fetching latest cars");
        return carRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * Get dealer statistics
     */
    public DashboardStats getDealerStats(Long dealerId) {
        log.info("Calculating stats for dealer: {}", dealerId);
        
        Long activeListings = carRepository.countByDealerIdAndActiveTrue(dealerId);
        Long totalViews = carRepository.sumViewsByDealerId(dealerId);
        if (totalViews == null) {
            totalViews = 0L;
        }
        
        List<Car> recentListings = carRepository.findByDealerIdOrderByCreatedAtDesc(dealerId)
            .stream()
            .limit(5)
            .toList();
        
        Long totalListings = (long) carRepository.findByDealerIdOrderByCreatedAtDesc(dealerId).size();
        Map<com.cardealer.model.enums.VehicleCategory, Long> listingsByCategory = new EnumMap<>(com.cardealer.model.enums.VehicleCategory.class);
        for (com.cardealer.model.enums.VehicleCategory category : com.cardealer.model.enums.VehicleCategory.values()) {
            Long categoryCount = carRepository.countByDealerIdAndCategoryAndActiveTrue(dealerId, category);
            listingsByCategory.put(category, categoryCount == null ? 0L : categoryCount);
        }

        return new DashboardStats(activeListings, totalViews, totalListings, recentListings, listingsByCategory);
    }

    /**
     * Get all cars (for backward compatibility)
     */
    public List<Car> getAllCars() {
        return carRepository.findAllByActiveTrue();
    }

    /**
     * Get car by ID (without incrementing views)
     */
    public Optional<Car> getCarByIdWithoutIncrement(Long id) {
        return carRepository.findById(id);
    }

    /**
     * Get featured cars (first 6 cars for home page)
     */
    public List<Car> getFeaturedCars() {
        List<Car> allCars = carRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
        return allCars.size() > 6 ? allCars.subList(0, 6) : allCars;
    }

    /**
     * Get cars by make
     */
    public List<Car> getCarsByMake(String make) {
        return carRepository.findByMakeIgnoreCaseAndActiveTrue(make);
    }

    public List<Car> getCarsByLocale(String locale) {
        return carRepository.findByLocaleIgnoreCaseAndActiveTrueOrderByCreatedAtDesc(locale);
    }

    public Page<Car> findCarsByCategory(VehicleCategory category, Pageable pageable) {
        return carRepository.findByCategoryAndActiveTrue(category, pageable);
    }

    public Page<Car> getDamagedVehicles(Pageable pageable) {
        return findCarsByCategory(VehicleCategory.DAMAGED, pageable);
    }

    public Page<Car> getSalvageVehicles(Pageable pageable) {
        return findCarsByCategory(VehicleCategory.SALVAGE, pageable);
    }

    public void validateImageCount(List<MultipartFile> images) {
        fileUploadUtil.validateImageCount(images);
    }

    /**
     * Get cars by year
     */
    public List<Car> getCarsByYear(Integer year) {
        return carRepository.findByYearAndActiveTrue(year);
    }

    /**
     * Get cars ordered by price (ascending)
     */
    public List<Car> getCarsOrderedByPriceAsc() {
        return carRepository.findAllByActiveTrueOrderByPriceAsc();
    }

    /**
     * Get cars ordered by price (descending)
     */
    public List<Car> getCarsOrderedByPriceDesc() {
        return carRepository.findAllByActiveTrueOrderByPriceDesc();
    }

    /**
     * Get cars ordered by year (newest first)
     */
    public List<Car> getCarsOrderedByYearDesc() {
        return carRepository.findAllByActiveTrueOrderByYearDesc();
    }

    /**
     * Save a car
     */
    @Transactional
    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    /**
     * Get total count of cars
     */
    public long getTotalCarCount() {
        return carRepository.countByActiveTrue();
    }

    public List<String> getAvailableBrands() {
        return carRepository.findDistinctMakesByActiveTrue();
    }

    // Helper methods

    /**
     * Map DTO to entity
     */
    private void mapDtoToEntity(CarDTO dto, Car car) {
        car.setMake(dto.getBrand());
        car.setModel(dto.getModel());
        car.setVariant(dto.getVariant());
        car.setYear(dto.getYear());
        car.setPrice(dto.getPrice());
        car.setExportPrice(dto.getExportPrice());
        car.setMileage(dto.getMileage());
        car.setColor(dto.getColor());
        car.setColorCode(dto.getColorCode());
        car.setDoors(dto.getDoors());
        car.setEngine(dto.getEngine());
        car.setPowerHp(dto.getPowerHp());
        car.setRefinedFuelType(dto.getRefinedFuelType());
        car.setRegistrationAvailable(dto.getRegistrationAvailable());
        car.setAwaitingVerification(dto.getAwaitingVerification());
        car.setFullInstructionBooklet(dto.getFullInstructionBooklet());
        car.setAllKeysAvailable(dto.getAllKeysAvailable());
        car.setEngineDamage(dto.getEngineDamage());
        car.setLowerDamage(dto.getLowerDamage());
        car.setDrivable(dto.getDrivable());
        car.setMovable(dto.getMovable());
        car.setEngineRuns(dto.getEngineRuns());
        car.setAirbagsIntact(dto.getAirbagsIntact());
        car.setDescription(dto.getDescription());
        
        // Convert string enums to enum types
        if (dto.getFuelType() != null) {
            car.setFuelType(FuelType.valueOf(dto.getFuelType().toUpperCase()));
        }
        if (dto.getTransmission() != null) {
            car.setTransmission(TransmissionType.valueOf(dto.getTransmission().toUpperCase()));
        }
        if (dto.getBodyType() != null) {
            car.setBodyType(BodyType.valueOf(dto.getBodyType().toUpperCase()));
        }
        if (dto.getCondition() != null) {
            car.setCondition(CarCondition.valueOf(dto.getCondition().toUpperCase()));
        }
        if (dto.getCategory() != null) {
            car.setCategory(VehicleCategory.valueOf(dto.getCategory().toUpperCase()));
        } else if (car.getCategory() == null) {
            car.setCategory(VehicleCategory.PASSENGER_CAR);
        }
        if (dto.getOrigin() != null && !dto.getOrigin().isBlank()) {
            car.setOrigin(VehicleOrigin.valueOf(dto.getOrigin().toUpperCase()));
        } else {
            car.setOrigin(null);
        }
        if (dto.getLocale() != null && !dto.getLocale().isBlank()) {
            car.setLocale(dto.getLocale().toLowerCase());
        } else if (car.getLocale() == null || car.getLocale().isBlank()) {
            car.setLocale("es");
        }
        
        // Set features
        if (dto.getFeatures() != null) {
            car.setFeatures(dto.getFeatures());
        }
        
        // Handle images - will be set separately by FileUploadUtil
        if (dto.getExistingImages() != null) {
            car.setImages(dto.getExistingImages());
        }
    }

    private void handleImages(CarDTO dto, Car car, boolean isUpdate) throws IOException {
        List<String> images = dto.getExistingImages() != null
            ? new ArrayList<>(dto.getExistingImages())
            : new java.util.ArrayList<>();

        List<MultipartFile> imageFiles = dto.getImageFiles();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            fileUploadUtil.validateImageCount(imageFiles);
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    try {
                        images.add(fileUploadUtil.saveFile(file));
                    } catch (IOException ex) {
                        throw new ImageProcessingException("No se pudo procesar la imagen " + file.getOriginalFilename(), ex);
                    }
                }
            }
        }

        if (!isUpdate && images.isEmpty()) {
            throw new IllegalArgumentException("Debe subir entre 20 y 25 imágenes");
        }

        if (isUpdate && images.isEmpty() && (car.getImages() == null || car.getImages().isEmpty())) {
            throw new IllegalArgumentException("Debe mantener o subir entre 20 y 25 imágenes");
        }

        if (!images.isEmpty()) {
            int imageCount = images.size();
            if (imageCount < FileUploadUtil.MIN_IMAGE_COUNT || imageCount > FileUploadUtil.MAX_IMAGE_COUNT) {
                throw new IllegalArgumentException("Cada vehículo debe tener entre 20 y 25 imágenes");
            }
            car.setImages(new ArrayList<>(new ArrayDeque<>(images)));
        }
    }

    /**
     * Get Sort object from sort string
     */
    private Sort getSortFromString(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "mileage_asc" -> Sort.by(Sort.Direction.ASC, "mileage");
            case "year_desc" -> Sort.by(Sort.Direction.DESC, "year");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}

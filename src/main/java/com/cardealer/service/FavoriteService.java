package com.cardealer.service;

import com.cardealer.exception.ResourceNotFoundException;
import com.cardealer.model.Car;
import com.cardealer.model.Favorite;
import com.cardealer.model.User;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.FavoriteRepository;
import com.cardealer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    /**
     * Add a car to favorites
     */
    @Transactional
    public Favorite addFavorite(Long userId, Long carId) {
        log.info("Adding car {} to favorites for user {}", carId, userId);
        
        // Check if already exists
        if (favoriteRepository.existsByUserIdAndCarId(userId, carId)) {
            log.warn("Car {} is already in favorites for user {}", carId, userId);
            return favoriteRepository.findByUserIdAndCarId(userId, carId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorito no encontrado"));
        }
        
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        
        // Validate car exists
        Car car = carRepository.findById(carId)
            .orElseThrow(() -> new ResourceNotFoundException("Coche no encontrado con id: " + carId));
        
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setCar(car);
        
        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("Favorite added successfully with id: {}", savedFavorite.getId());
        
        return savedFavorite;
    }

    /**
     * Remove a car from favorites
     */
    @Transactional
    public void removeFavorite(Long userId, Long carId) {
        log.info("Removing car {} from favorites for user {}", carId, userId);
        
        if (!favoriteRepository.existsByUserIdAndCarId(userId, carId)) {
            log.warn("Car {} is not in favorites for user {}", carId, userId);
            throw new ResourceNotFoundException("Favorito no encontrado");
        }
        
        favoriteRepository.deleteByUserIdAndCarId(userId, carId);
        log.info("Favorite removed successfully");
    }

    /**
     * Get user's favorite cars
     */
    public List<Car> getUserFavorites(Long userId) {
        log.info("Fetching favorites for user: {}", userId);
        
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return favorites.stream()
            .map(Favorite::getCar)
            .collect(Collectors.toList());
    }

    public Page<Favorite> getUserFavorites(Long userId, Pageable pageable) {
        log.info("Fetching paginated favorites for user: {}", userId);
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public long countUserFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
    }

    /**
     * Check if a car is in user's favorites
     */
    public Boolean isFavorite(Long userId, Long carId) {
        log.debug("Checking if car {} is favorite for user {}", carId, userId);
        return favoriteRepository.existsByUserIdAndCarId(userId, carId);
    }

    /**
     * Get all favorites for a user (with Favorite entities)
     */
    public List<Favorite> getUserFavoriteEntities(Long userId) {
        log.info("Fetching favorite entities for user: {}", userId);
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

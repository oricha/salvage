package com.cardealer.service;

import com.cardealer.exception.ResourceNotFoundException;
import com.cardealer.model.Dealer;
import com.cardealer.model.User;
import com.cardealer.repository.DealerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class DealerService {
    
    @Autowired
    private DealerRepository dealerRepository;
    
    /**
     * Create a new dealer profile
     */
    @CacheEvict(value = "activeDealers", allEntries = true)
    public Dealer createDealer(Dealer dealer, User user) {
        log.info("Creating dealer profile for user: {}", user.getEmail());
        
        dealer.setUser(user);
        dealer.setActive(true);
        
        dealer = dealerRepository.save(dealer);
        log.info("Dealer created successfully with ID: {}", dealer.getId());
        
        return dealer;
    }
    
    /**
     * Update dealer information
     */
    @CacheEvict(value = "activeDealers", allEntries = true)
    public Dealer updateDealer(Long id, Dealer dealerDetails) {
        log.info("Updating dealer with ID: {}", id);
        
        Dealer dealer = getDealerById(id);
        
        dealer.setName(dealerDetails.getName());
        dealer.setPhone(dealerDetails.getPhone());
        dealer.setAddress(dealerDetails.getAddress());
        dealer.setCity(dealerDetails.getCity());
        dealer.setPostalCode(dealerDetails.getPostalCode());
        dealer.setDescription(dealerDetails.getDescription());
        dealer.setLogoUrl(dealerDetails.getLogoUrl());
        dealer.setBannerUrl(dealerDetails.getBannerUrl());
        
        // Email cannot be changed through this method
        
        dealer = dealerRepository.save(dealer);
        log.info("Dealer updated successfully with ID: {}", id);
        
        return dealer;
    }
    
    /**
     * Get dealer by ID
     */
    @Cacheable("dealers")
    public Dealer getDealerById(Long id) {
        log.debug("Fetching dealer by ID: {}", id);
        return dealerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Concesionario no encontrado con ID: " + id));
    }
    
    /**
     * Get all active dealers
     */
    @Cacheable("activeDealers")
    public List<Dealer> getAllActiveDealers() {
        log.debug("Fetching all active dealers");
        return dealerRepository.findByActiveTrue();
    }
    
    /**
     * Get dealer by user ID
     */
    public Dealer getDealerByUserId(Long userId) {
        log.debug("Fetching dealer by user ID: {}", userId);
        return dealerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Concesionario no encontrado para el usuario con ID: " + userId));
    }
    
    /**
     * Deactivate dealer (soft delete)
     */
    @CacheEvict(value = "activeDealers", allEntries = true)
    public void deactivateDealer(Long id) {
        log.info("Deactivating dealer with ID: {}", id);
        
        Dealer dealer = getDealerById(id);
        dealer.setActive(false);
        dealerRepository.save(dealer);
        
        log.info("Dealer deactivated successfully with ID: {}", id);
    }
    
    /**
     * Activate dealer
     */
    @CacheEvict(value = "activeDealers", allEntries = true)
    public void activateDealer(Long id) {
        log.info("Activating dealer with ID: {}", id);
        
        Dealer dealer = getDealerById(id);
        dealer.setActive(true);
        dealerRepository.save(dealer);
        
        log.info("Dealer activated successfully with ID: {}", id);
    }
}

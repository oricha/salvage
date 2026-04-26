package com.cardealer.service;

import com.cardealer.catalog.DealerDirectoryCatalog;
import com.cardealer.dto.DealerDirectoryEntry;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<DealerDirectoryEntry> getDealerDirectoryEntries(String query, String region) {
        Map<String, DealerDirectoryEntry> merged = new LinkedHashMap<>();

        DealerDirectoryCatalog.entries().forEach(entry ->
            merged.put(normalizeKey(entry.getCompanyName()), entry)
        );
        getAllActiveDealers().stream()
            .map(this::buildDirectoryEntry)
            .forEach(entry -> merged.put(normalizeKey(entry.getCompanyName()), entry));

        return merged.values().stream()
            .filter(entry -> matchesQuery(entry, query))
            .filter(entry -> matchesRegion(entry, region))
            .sorted(Comparator.comparing(DealerDirectoryEntry::getCompanyName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public Map<String, List<DealerDirectoryEntry>> getDealerDirectoryByLetter(String query, String region) {
        Map<String, List<DealerDirectoryEntry>> grouped = new LinkedHashMap<>();
        getDealerDirectoryEntries(query, region).forEach(entry ->
            grouped.computeIfAbsent(entry.getAlphabetLetter(), key -> new ArrayList<>()).add(entry)
        );
        return grouped;
    }

    public List<String> getDealerSearchOptions() {
        return getDealerDirectoryEntries(null, null).stream()
            .map(DealerDirectoryEntry::getCompanyName)
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
    }

    public List<String> getDealerRegions() {
        return DealerDirectoryCatalog.supportedRegions();
    }

    public Map<String, List<String>> getDealerNamesByLetter() {
        return getDealerDirectoryEntries(null, null).stream()
            .collect(Collectors.groupingBy(
                DealerDirectoryEntry::getAlphabetLetter,
                LinkedHashMap::new,
                Collectors.mapping(DealerDirectoryEntry::getCompanyName, Collectors.toList())
            ));
    }

    public DealerDirectoryEntry buildDirectoryEntry(Dealer dealer) {
        String region = DealerDirectoryCatalog.regionForCity(dealer.getCity());
        return DealerDirectoryEntry.builder()
            .dealerId(dealer.getId())
            .companyName(dealer.getName())
            .specialization(deriveSpecialization(dealer))
            .region(region)
            .city(dealer.getCity())
            .email(dealer.getEmail())
            .phone(dealer.getPhone())
            .logoUrl(resolveMediaPath(dealer.getLogoUrl(), "/img/dealer/01.png"))
            .listingCount(dealer.getCars() != null ? dealer.getCars().size() : 0)
            .realDealer(true)
            .build();
    }

    public String deriveSpecialization(Dealer dealer) {
        String description = Optional.ofNullable(dealer.getDescription()).orElse("").toLowerCase(Locale.ROOT);
        if (description.contains("desguace") || description.contains("recicl")) {
            return "Desguace";
        }
        if (description.contains("alta gama") || description.contains("premium") || description.contains("deportivo")) {
            return "Motex Premium";
        }
        if (description.contains("ocasion")) {
            return "Ocasion";
        }
        if (description.contains("comercial") || description.contains("industrial")) {
            return "Vehiculo comercial";
        }
        return "Motex";
    }

    public String resolveMediaPath(String path, String fallback) {
        if (path == null || path.isBlank()) {
            return fallback;
        }
        if (path.startsWith("/")) {
            return path;
        }
        return "/uploads/" + path;
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

    private boolean matchesQuery(DealerDirectoryEntry entry, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(entry.getCompanyName(), needle)
            || containsIgnoreCase(entry.getSpecialization(), needle)
            || containsIgnoreCase(entry.getRegion(), needle)
            || containsIgnoreCase(entry.getCity(), needle);
    }

    private boolean matchesRegion(DealerDirectoryEntry entry, String region) {
        if (region == null || region.isBlank()) {
            return true;
        }
        return region.trim().equalsIgnoreCase(Optional.ofNullable(entry.getRegion()).orElse(""));
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private String normalizeKey(String value) {
        return Optional.ofNullable(value)
            .map(candidate -> candidate.trim().toLowerCase(Locale.ROOT))
            .orElse("");
    }
}

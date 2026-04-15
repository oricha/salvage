package com.cardealer.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Value("${marketplace.cache.vehicles-ttl-minutes:5}")
    private long vehiclesTtlMinutes;

    @Value("${marketplace.cache.dealers-ttl-minutes:5}")
    private long dealersTtlMinutes;

    @Value("${marketplace.cache.i18n-ttl-minutes:30}")
    private long i18nTtlMinutes;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("latestCars", "activeDealers", "vehicles", "dealers", "i18n");
        cacheManager.registerCustomCache("latestCars", Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(vehiclesTtlMinutes)).maximumSize(100).build());
        cacheManager.registerCustomCache("activeDealers", Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(dealersTtlMinutes)).maximumSize(100).build());
        cacheManager.registerCustomCache("vehicles", Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(vehiclesTtlMinutes)).maximumSize(500).build());
        cacheManager.registerCustomCache("dealers", Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(dealersTtlMinutes)).maximumSize(200).build());
        cacheManager.registerCustomCache("i18n", Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(i18nTtlMinutes)).maximumSize(1000).build());
        return cacheManager;
    }
}

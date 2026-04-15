package com.cardealer.property;

import com.cardealer.config.CacheConfig;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Tag;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CacheBehaviorPropertiesTest {

    @Property
    @Tag("Feature: portal-venta-coches, Property 25: Cache Behavior")
    void cacheBehavior(@ForAll("cacheNames") String cacheName,
                       @ForAll("cacheKeys") String cacheKey,
                       @ForAll("cacheValues") String cacheValue) {
        CacheConfig config = new CacheConfig();
        ReflectionTestUtils.setField(config, "vehiclesTtlMinutes", 5L);
        ReflectionTestUtils.setField(config, "dealersTtlMinutes", 5L);
        ReflectionTestUtils.setField(config, "i18nTtlMinutes", 30L);

        CacheManager cacheManager = config.cacheManager();
        Cache cache = cacheManager.getCache(cacheName);

        assertNotNull(cache);
        cache.put(cacheKey, cacheValue);
        assertEquals(cacheValue, cache.get(cacheKey, String.class));

        cache.evict(cacheKey);
        assertNull(cache.get(cacheKey));
    }

    @Provide
    net.jqwik.api.Arbitrary<String> cacheNames() {
        return Arbitraries.of("latestCars", "activeDealers", "vehicles", "dealers", "i18n");
    }

    @Provide
    net.jqwik.api.Arbitrary<String> cacheKeys() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(16);
    }

    @Provide
    net.jqwik.api.Arbitrary<String> cacheValues() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(24);
    }
}

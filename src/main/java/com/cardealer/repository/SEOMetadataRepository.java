package com.cardealer.repository;

import com.cardealer.model.SEOMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SEOMetadataRepository extends JpaRepository<SEOMetadataEntity, Long> {

    Optional<SEOMetadataEntity> findByPageKeyAndLocale(String pageKey, String locale);
}

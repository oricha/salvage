package com.cardealer.repository;

import com.cardealer.model.LocalizedContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalizedContentRepository extends JpaRepository<LocalizedContent, Long> {

    Optional<LocalizedContent> findByContentKeyAndLocale(String contentKey, String locale);

    List<LocalizedContent> findByLocaleAndCategoryOrderByContentKeyAsc(String locale, String category);
}

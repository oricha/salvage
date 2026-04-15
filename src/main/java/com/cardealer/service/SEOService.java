package com.cardealer.service;

import com.cardealer.model.Car;
import com.cardealer.model.SEOMetadataEntity;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.SEOMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SEOService {

    private static final String BASE_URL = "https://portalcoches.com";

    private final SEOMetadataRepository seoMetadataRepository;
    private final CarRepository carRepository;

    @Transactional
    public SEOMetadataEntity generateCarMetadata(Car car, Locale locale) {
        String language = locale != null ? locale.getLanguage() : "es";
        String pageKey = "car:" + car.getId();
        SEOMetadataEntity metadata = seoMetadataRepository.findByPageKeyAndLocale(pageKey, language)
            .orElseGet(SEOMetadataEntity::new);

        String title = car.getMake() + " " + car.getModel() + " " + car.getYear() + " | " + car.getCategory();
        String description = buildCarDescription(car);
        String keywords = String.join(", ",
            List.of(car.getMake(), car.getModel(), car.getCategory().name(), car.getCondition().name(), "salvage cars"));

        metadata.setPageKey(pageKey);
        metadata.setLocale(language);
        metadata.setMetaTitle(title);
        metadata.setMetaDescription(description);
        metadata.setMetaKeywords(keywords);
        metadata.setOgTitle(title);
        metadata.setOgDescription(description);
        metadata.setOgImage(primaryImage(car));
        metadata.setStructuredData(generateStructuredData(car));
        return seoMetadataRepository.save(metadata);
    }

    @Transactional
    public SEOMetadataEntity generatePageMetadata(String pageKey, Locale locale) {
        String language = locale != null ? locale.getLanguage() : "es";
        SEOMetadataEntity metadata = seoMetadataRepository.findByPageKeyAndLocale(pageKey, language)
            .orElseGet(SEOMetadataEntity::new);

        metadata.setPageKey(pageKey);
        metadata.setLocale(language);
        metadata.setMetaTitle(defaultTitle(pageKey));
        metadata.setMetaDescription(defaultDescription(pageKey));
        metadata.setMetaKeywords(defaultKeywords(pageKey));
        metadata.setOgTitle(metadata.getMetaTitle());
        metadata.setOgDescription(metadata.getMetaDescription());
        metadata.setOgImage("/img/logo/logo-full-light.png");
        metadata.setStructuredData(defaultStructuredData(pageKey));
        return seoMetadataRepository.save(metadata);
    }

    public String generateStructuredData(Car car) {
        String image = primaryImage(car);
        return """
            {
              "@context":"https://schema.org",
              "@type":"Product",
              "name":"%s %s %s",
              "category":"%s",
              "description":"%s",
              "image":"%s",
              "offers":{
                "@type":"Offer",
                "price":"%s",
                "priceCurrency":"EUR",
                "availability":"https://schema.org/InStock",
                "url":"%s/cars/%d"
              }
            }
            """.formatted(
            escape(car.getMake()),
            escape(car.getModel()),
            car.getYear(),
            escape(car.getCategory().name()),
            escape(buildCarDescription(car)),
            image,
            car.getPrice(),
            BASE_URL,
            car.getId()
        ).replace("\n", "");
    }

    public String generateSitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        appendUrl(xml, BASE_URL + "/", null);
        appendUrl(xml, BASE_URL + "/cars", null);
        appendUrl(xml, BASE_URL + "/about", null);
        appendUrl(xml, BASE_URL + "/contact", null);
        carRepository.findAllByActiveTrue().forEach(car ->
            appendUrl(xml, BASE_URL + "/cars/" + car.getId(), car.getUpdatedAt())
        );
        xml.append("</urlset>");
        return xml.toString();
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void refreshSitemapMetadata() {
        carRepository.findAllByActiveTrue().forEach(car -> generateCarMetadata(car, Locale.forLanguageTag(car.getLocale())));
        generatePageMetadata("home", Locale.forLanguageTag("es"));
        generatePageMetadata("inventory", Locale.forLanguageTag("es"));
    }

    public Map<String, Object> toModelAttributes(SEOMetadataEntity metadata, String url) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("title", metadata.getMetaTitle());
        attributes.put("pageDescription", metadata.getMetaDescription());
        attributes.put("pageKeywords", metadata.getMetaKeywords());
        attributes.put("ogTitle", metadata.getOgTitle());
        attributes.put("ogDescription", metadata.getOgDescription());
        attributes.put("ogImage", metadata.getOgImage());
        attributes.put("structuredData", metadata.getStructuredData());
        attributes.put("ogUrl", url);
        return attributes;
    }

    private void appendUrl(StringBuilder xml, String location, java.time.LocalDateTime updatedAt) {
        xml.append("<url><loc>").append(location).append("</loc>");
        if (updatedAt != null) {
            xml.append("<lastmod>").append(updatedAt.format(DateTimeFormatter.ISO_DATE)).append("</lastmod>");
        }
        xml.append("</url>");
    }

    private String buildCarDescription(Car car) {
        return (car.getDescription() != null && !car.getDescription().isBlank())
            ? car.getDescription()
            : "Vehículo " + car.getCategory().name().toLowerCase(Locale.ROOT) + " disponible con " + car.getMileage() + " km.";
    }

    private String primaryImage(Car car) {
        return car.getImages() != null && !car.getImages().isEmpty()
            ? "/uploads/" + car.getImages().get(0)
            : "/img/car/01.jpg";
    }

    private String defaultTitle(String pageKey) {
        return switch (pageKey) {
            case "home" -> "Portal de Coches | Vehículos dañados, salvage y ocasión";
            case "inventory" -> "Inventario de Vehículos | Portal de Coches";
            default -> "Portal de Coches";
        };
    }

    private String defaultDescription(String pageKey) {
        return switch (pageKey) {
            case "home" -> "Marketplace de vehículos dañados, salvage, turismos, comerciales y motos.";
            case "inventory" -> "Explora el inventario completo con filtros por categoría, condición y kilometraje.";
            default -> "Portal de Coches";
        };
    }

    private String defaultKeywords(String pageKey) {
        return switch (pageKey) {
            case "home" -> "coches salvage, coches dañados, marketplace vehículos";
            case "inventory" -> "inventario coches, filtros coches, vehículos salvage";
            default -> "portal coches";
        };
    }

    private String defaultStructuredData(String pageKey) {
        return "{\"@context\":\"https://schema.org\",\"@type\":\"WebPage\",\"name\":\"" + escape(defaultTitle(pageKey)) + "\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}

package com.cardealer.controller;

import com.cardealer.catalog.AdvancedSearchCatalog;
import com.cardealer.dto.CarFilterDTO;
import com.cardealer.dto.BreadcrumbItem;
import com.cardealer.dto.MessageDTO;
import com.cardealer.model.Car;
import com.cardealer.model.Comment;
import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.FuelType;
import com.cardealer.model.enums.TransmissionType;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.service.CarService;
import com.cardealer.service.CommentService;
import com.cardealer.service.FavoriteService;
import com.cardealer.service.RecentlyViewedService;
import com.cardealer.service.SEOService;
import com.cardealer.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final CommentService commentService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final RecentlyViewedService recentlyViewedService;
    private final SEOService seoService;

    /**
     * List cars with filters and pagination
     */
    @GetMapping
    public String listCars(
            @ModelAttribute CarFilterDTO filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        buildCarsListing(filters, page, size, model);
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generatePageMetadata("inventory", Locale.forLanguageTag(filters.getLocale() != null ? filters.getLocale() : "es")),
            "/cars"
        ));
        return "inventory-grid";
    }

    @GetMapping("/list")
    public String listCarsAsList(
            @ModelAttribute CarFilterDTO filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        buildCarsListing(filters, page, size, model);
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generatePageMetadata("inventory", Locale.forLanguageTag(filters.getLocale() != null ? filters.getLocale() : "es")),
            "/cars/list"
        ));
        return "inventory-list";
    }

    @GetMapping("/damaged")
    public String damagedCars(@ModelAttribute CarFilterDTO filters,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size,
                              Model model) {
        filters.setCategories(List.of(VehicleCategory.DAMAGED));
        return listCars(filters, page, size, model);
    }

    @GetMapping("/salvage")
    public String salvageCars(@ModelAttribute CarFilterDTO filters,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size,
                              Model model) {
        filters.setCategories(List.of(VehicleCategory.SALVAGE));
        return listCars(filters, page, size, model);
    }

    @GetMapping("/category/{category}")
    public String carsByCategory(@PathVariable VehicleCategory category,
                                 @ModelAttribute CarFilterDTO filters,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 Model model) {
        filters.setCategories(List.of(category));
        return listCars(filters, page, size, model);
    }

    @GetMapping("/locale/{locale}")
    public String carsByLocale(@PathVariable String locale,
                               @ModelAttribute CarFilterDTO filters,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size,
                               Model model) {
        filters.setLocale(locale);
        return listCars(filters, page, size, model);
    }

    /**
     * Car detail page
     */
    @GetMapping("/{id}")
    public String carDetail(@PathVariable Long id, Model model, Authentication authentication, HttpSession session, HttpServletRequest request) {
        // Get car and increment views
        Car car = carService.getCarById(id);
        var currentUser = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())
            ? userService.getUserByEmail(authentication.getName())
            : null;
        recentlyViewedService.addToRecentlyViewed(id, session, currentUser, request);
        model.addAttribute("car", car);
        model.addAttribute("breadcrumbItems", List.of(
            new BreadcrumbItem("Inicio", "/", false),
            new BreadcrumbItem("Inventario", "/cars", false),
            new BreadcrumbItem(car.getMake() + " " + car.getModel(), null, true)
        ));
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generateCarMetadata(car, Locale.forLanguageTag(car.getLocale())),
            "/cars/" + car.getId()
        ));
        
        // Get related cars (same brand)
        List<Car> relatedCars = carService.getRelatedCars(id);
        model.addAttribute("relatedCars", relatedCars);
        
        // Get comments for this car
        List<Comment> comments = commentService.getCarComments(id);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentService.getCommentCount(id));
        
        // Add message DTO for contact form
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setCarId(id);
        if (car.getDealer() != null && car.getDealer().getUser() != null) {
            messageDTO.setReceiverId(car.getDealer().getUser().getId());
        }
        model.addAttribute("messageDTO", messageDTO);
        
        // Check if car is in user's favorites (if authenticated)
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            boolean isFavorite = favoriteService.isFavorite(currentUser.getId(), id);
            model.addAttribute("isFavorite", isFavorite);
        } else {
            model.addAttribute("isFavorite", false);
        }
        
        return "inventory-single";
    }

    /**
     * Compare cars page
     */
    @GetMapping("/compare")
    public String compareCars(@RequestParam(required = false) List<Long> ids, Model model) {
        if (ids == null || ids.isEmpty()) {
            // Redirect to cars list if no IDs provided
            return "redirect:/cars";
        }
        
        // Limit to maximum 3 cars
        if (ids.size() > 3) {
            ids = ids.subList(0, 3);
        }
        
        // Get cars by IDs
        List<Car> carsToCompare = ids.stream()
            .map(carService::getCarById)
            .collect(Collectors.toList());
        
        model.addAttribute("cars", carsToCompare);
        model.addAttribute("pageDescription", "Compara varios coches en paralelo para tomar una mejor decisión de compra.");
        model.addAttribute("pageKeywords", "comparar coches, comparativa vehículos");
        model.addAttribute("ogTitle", "Comparador de Coches");
        
        return "compare";
    }

    private Page<Car> buildCarsListing(CarFilterDTO filters, int page, int size, Model model) {
        normalizeAdvancedSearchFilters(filters);
        Pageable pageable = PageRequest.of(page, size);
        Page<Car> carsPage = carService.findCarsWithFilters(filters, pageable);

        model.addAttribute("cars", carsPage);
        model.addAttribute("filters", filters);
        model.addAttribute("availableBrands", AdvancedSearchCatalog.mergedBrands(carService.getAvailableBrands()));
        model.addAttribute("modelsByMake", AdvancedSearchCatalog.modelsByBrand());
        model.addAttribute("fuelTypes", Arrays.asList(FuelType.values()));
        model.addAttribute("transmissionTypes", Arrays.asList(TransmissionType.values()));
        model.addAttribute("bodyTypes", Arrays.asList(BodyType.values()));
        model.addAttribute("conditions", Arrays.asList(CarCondition.values()));
        model.addAttribute("categories", Arrays.asList(VehicleCategory.values()));
        model.addAttribute("sidebarFuelOptions", AdvancedSearchCatalog.fuelOptions());
        model.addAttribute("sidebarTransmissionOptions", AdvancedSearchCatalog.transmissionOptions());
        model.addAttribute("sidebarColorOptions", AdvancedSearchCatalog.colorOptions());
        model.addAttribute("sidebarColorCodeOptions", AdvancedSearchCatalog.colorCodeOptions());
        model.addAttribute("sidebarBodyTypeOptions", AdvancedSearchCatalog.bodyTypeOptions());
        model.addAttribute("sidebarOriginOptions", AdvancedSearchCatalog.originOptions());
        model.addAttribute("sidebarNearbyRadiusOptions", AdvancedSearchCatalog.nearbyRadiusOptions());
        model.addAttribute("sidebarPricePresets", AdvancedSearchCatalog.pricePresets());
        model.addAttribute("sidebarMileagePresets", AdvancedSearchCatalog.mileagePresets());
        model.addAttribute("advancedFiltersExpanded", hasAdvancedFilters(filters));
        model.addAttribute("gridViewUrl", buildViewUrl("/cars", filters));
        model.addAttribute("listViewUrl", buildViewUrl("/cars/list", filters));
        model.addAttribute("extraStylesheets", List.of("/css/inventory-sidebar.css"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carsPage.getTotalPages());
        model.addAttribute("totalItems", carsPage.getTotalElements());
        return carsPage;
    }

    private void normalizeAdvancedSearchFilters(CarFilterDTO filters) {
        if (filters.getYearFrom() != null) {
            filters.setYearFrom(Math.max(1936, Math.min(2026, filters.getYearFrom())));
        }
        if (filters.getYearTo() != null) {
            filters.setYearTo(Math.max(1936, Math.min(2026, filters.getYearTo())));
        }
        if (filters.getYearFrom() != null && filters.getYearTo() != null && filters.getYearFrom() > filters.getYearTo()) {
            filters.setYearTo(filters.getYearFrom());
        }

        if (filters.getMinPrice() != null && filters.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
            filters.setMinPrice(BigDecimal.ZERO);
        }
        if (filters.getMaxPrice() != null && filters.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
            filters.setMaxPrice(BigDecimal.ZERO);
        }
        if (filters.getMinPrice() != null && filters.getMaxPrice() != null && filters.getMinPrice().compareTo(filters.getMaxPrice()) > 0) {
            filters.setMaxPrice(filters.getMinPrice());
        }

        if (filters.getMinMileage() != null && filters.getMinMileage() < 0) {
            filters.setMinMileage(0);
        }
        if (filters.getMaxMileage() != null && filters.getMaxMileage() < 0) {
            filters.setMaxMileage(0);
        }
        if (filters.getMinMileage() != null && filters.getMaxMileage() != null && filters.getMinMileage() > filters.getMaxMileage()) {
            filters.setMaxMileage(filters.getMinMileage());
        }

        if (filters.getBrands() != null) {
            filters.setBrands(filters.getBrands().stream().filter(brand -> brand != null && !brand.isBlank()).toList());
        }

        Map<String, List<String>> modelsByBrand = AdvancedSearchCatalog.modelsByBrand();
        if (filters.getModel() != null && !filters.getModel().isBlank()) {
            if (filters.getBrands() == null || filters.getBrands().isEmpty()) {
                filters.setModel(null);
            } else {
                String selectedBrand = filters.getBrands().get(0);
                List<String> validModels = modelsByBrand.getOrDefault(selectedBrand, List.of());
                if (!validModels.contains(filters.getModel())) {
                    filters.setModel(null);
                }
            }
        }

        if (filters.getColor() != null && !AdvancedSearchCatalog.supportedColorValues().contains(filters.getColor())) {
            filters.setColor(null);
        }

        if (filters.getColorCode() != null) {
            filters.setColorCode(filters.getColorCode().trim().toUpperCase(Locale.ROOT));
            if (!filters.getColorCode().isEmpty() && !AdvancedSearchCatalog.supportedColorCodes().contains(filters.getColorCode())) {
                filters.setColorCode(null);
            }
        }

        if (filters.getBodyTypes() != null) {
            Set<String> supportedBodyTypes = AdvancedSearchCatalog.supportedBodyTypeValues();
            filters.setBodyTypes(filters.getBodyTypes().stream()
                .filter(bodyType -> bodyType != null && supportedBodyTypes.contains(bodyType))
                .distinct()
                .toList());
        }

        if (filters.getOrigins() != null) {
            Set<String> supportedOrigins = AdvancedSearchCatalog.supportedOriginValues();
            filters.setOrigins(filters.getOrigins().stream()
                .filter(origin -> origin != null && supportedOrigins.contains(origin))
                .distinct()
                .toList());
        }

        if (filters.getNearbyRadiusKm() != null && !AdvancedSearchCatalog.supportedNearbyRadiusOptions().contains(filters.getNearbyRadiusKm())) {
            filters.setNearbyRadiusKm(null);
        }
        if (filters.getReferenceLatitude() != null && (filters.getReferenceLatitude() < -90 || filters.getReferenceLatitude() > 90)) {
            filters.setReferenceLatitude(null);
        }
        if (filters.getReferenceLongitude() != null && (filters.getReferenceLongitude() < -180 || filters.getReferenceLongitude() > 180)) {
            filters.setReferenceLongitude(null);
        }
    }

    private boolean hasAdvancedFilters(CarFilterDTO filters) {
        return filters.getMinPrice() != null
            || filters.getMaxPrice() != null
            || (filters.getColor() != null && !filters.getColor().isBlank())
            || (filters.getColorCode() != null && !filters.getColorCode().isBlank())
            || (filters.getBodyTypes() != null && !filters.getBodyTypes().isEmpty())
            || filters.getRefinedFuelType() != null
            || filters.getMinMileage() != null
            || filters.getMaxMileage() != null
            || (filters.getOrigins() != null && !filters.getOrigins().isEmpty())
            || filters.getNearbyRadiusKm() != null
            || filters.getRegistrationAvailable() != null
            || Boolean.TRUE.equals(filters.getAwaitingVerification())
            || filters.getFullInstructionBooklet() != null
            || filters.getAllKeysAvailable() != null
            || filters.getEngineDamage() != null
            || filters.getLowerDamage() != null
            || filters.getDrivable() != null
            || filters.getMovable() != null
            || filters.getEngineRuns() != null
            || filters.getAirbagsIntact() != null;
    }

    private String buildViewUrl(String targetPath, CarFilterDTO filters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(targetPath);
        addQueryParam(builder, "brands", filters.getBrands());
        addQueryParam(builder, "model", filters.getModel());
        addQueryParam(builder, "yearFrom", filters.getYearFrom());
        addQueryParam(builder, "yearTo", filters.getYearTo());
        addQueryParam(builder, "minPrice", filters.getMinPrice());
        addQueryParam(builder, "maxPrice", filters.getMaxPrice());
        addQueryParam(builder, "color", filters.getColor());
        addQueryParam(builder, "colorCode", filters.getColorCode());
        addQueryParam(builder, "fuelTypes", filters.getFuelTypes());
        addQueryParam(builder, "transmissions", filters.getTransmissions());
        addQueryParam(builder, "bodyTypes", filters.getBodyTypes());
        addQueryParam(builder, "refinedFuelType", filters.getRefinedFuelType() != null ? filters.getRefinedFuelType().name() : null);
        addQueryParam(builder, "minMileage", filters.getMinMileage());
        addQueryParam(builder, "maxMileage", filters.getMaxMileage());
        addQueryParam(builder, "origins", filters.getOrigins());
        addQueryParam(builder, "nearbyRadiusKm", filters.getNearbyRadiusKm());
        addQueryParam(builder, "referenceLatitude", filters.getReferenceLatitude());
        addQueryParam(builder, "referenceLongitude", filters.getReferenceLongitude());
        addQueryParam(builder, "registrationAvailable", filters.getRegistrationAvailable() != null ? filters.getRegistrationAvailable().name() : null);
        addQueryParam(builder, "awaitingVerification", Boolean.TRUE.equals(filters.getAwaitingVerification()) ? true : null);
        addQueryParam(builder, "fullInstructionBooklet", filters.getFullInstructionBooklet() != null ? filters.getFullInstructionBooklet().name() : null);
        addQueryParam(builder, "allKeysAvailable", filters.getAllKeysAvailable() != null ? filters.getAllKeysAvailable().name() : null);
        addQueryParam(builder, "engineDamage", filters.getEngineDamage() != null ? filters.getEngineDamage().name() : null);
        addQueryParam(builder, "lowerDamage", filters.getLowerDamage() != null ? filters.getLowerDamage().name() : null);
        addQueryParam(builder, "drivable", filters.getDrivable() != null ? filters.getDrivable().name() : null);
        addQueryParam(builder, "movable", filters.getMovable() != null ? filters.getMovable().name() : null);
        addQueryParam(builder, "engineRuns", filters.getEngineRuns() != null ? filters.getEngineRuns().name() : null);
        addQueryParam(builder, "airbagsIntact", filters.getAirbagsIntact() != null ? filters.getAirbagsIntact().name() : null);
        addQueryParam(builder, "conditions", filters.getConditions() != null ? filters.getConditions().stream().map(Enum::name).toList() : null);
        addQueryParam(builder, "categories", filters.getCategories() != null ? filters.getCategories().stream().map(Enum::name).toList() : null);
        addQueryParam(builder, "searchText", filters.getSearchText());
        addQueryParam(builder, "searchQuery", filters.getSearchQuery());
        addQueryParam(builder, "locale", filters.getLocale());
        return builder.toUriString();
    }

    private void addQueryParam(UriComponentsBuilder builder, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof List<?> values) {
            values.stream()
                .filter(item -> item != null && !item.toString().isBlank())
                .forEach(item -> builder.queryParam(key, item));
            return;
        }
        if (!value.toString().isBlank()) {
            builder.queryParam(key, value);
        }
    }
}

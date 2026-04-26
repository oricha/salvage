package com.cardealer.integration;

import com.cardealer.config.LocaleConfig;
import com.cardealer.config.LocaleInterceptor;
import com.cardealer.config.WebConfig;
import com.cardealer.controller.CarController;
import com.cardealer.controller.ContactController;
import com.cardealer.controller.DashboardController;
import com.cardealer.controller.DealerController;
import com.cardealer.controller.GlobalModelAttributesController;
import com.cardealer.controller.HomeController;
import com.cardealer.dto.CarFilterDTO;
import com.cardealer.dto.DealerDirectoryEntry;
import com.cardealer.model.Car;
import com.cardealer.model.Dealer;
import com.cardealer.model.User;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.TriStateOption;
import com.cardealer.model.enums.UserRole;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.service.CarService;
import com.cardealer.service.CommentService;
import com.cardealer.service.ContactService;
import com.cardealer.service.DealerService;
import com.cardealer.service.FavoriteService;
import com.cardealer.service.LocalizationService;
import com.cardealer.service.MessageService;
import com.cardealer.service.RecentlyViewedService;
import com.cardealer.service.SEOService;
import com.cardealer.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
@WebMvcTest(controllers = {DashboardController.class, HomeController.class, CarController.class, ContactController.class, DealerController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalModelAttributesController.class, LocaleConfig.class, LocaleInterceptor.class, WebConfig.class})
class PortalMarketplaceIntegrationTest {

    private static final String SELLER_EMAIL = "seller@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarService carService;
    @MockBean
    private DealerService dealerService;
    @MockBean
    private UserService userService;
    @MockBean
    private MessageService messageService;
    @MockBean
    private FavoriteService favoriteService;
    @MockBean
    private CommentService commentService;
    @MockBean
    private RecentlyViewedService recentlyViewedService;
    @MockBean
    private SEOService seoService;
    @MockBean
    private LocalizationService localizationService;
    @MockBean
    private ContactService contactService;

    @BeforeEach
    void setUp() throws Exception {
        when(localizationService.getSupportedLocales()).thenReturn(List.of(
            Locale.forLanguageTag("es"),
            Locale.ENGLISH,
            Locale.forLanguageTag("nl"),
            Locale.GERMAN,
            Locale.FRENCH
        ));
        when(localizationService.resolveLocale(any())).thenAnswer(invocation -> Locale.forLanguageTag("es"));
        when(seoService.generatePageMetadata(any(), any())).thenReturn(null);
        when(seoService.generateCarMetadata(any(), any())).thenReturn(null);
        when(seoService.toModelAttributes(any(), any())).thenReturn(Map.of());
        when(carService.getLatestCars()).thenReturn(List.of(sampleCar(1L, VehicleCategory.PASSENGER_CAR, CarCondition.OCASION)));
        when(carService.getFeaturedCars()).thenReturn(List.of(sampleCar(2L, VehicleCategory.DAMAGED, CarCondition.ACCIDENTADO)));
        when(carService.getTotalCarCount()).thenReturn(12L);
        when(carService.getAvailableBrands()).thenReturn(List.of("Ford", "Toyota"));
        when(carService.findCarsByCategory(any(), any())).thenAnswer(invocation -> {
            VehicleCategory category = invocation.getArgument(0);
            return new PageImpl<>(List.of(sampleCar(10L + category.ordinal(), category, CarCondition.OCASION)));
        });
        when(carService.getCarById(any())).thenAnswer(invocation -> sampleCar(invocation.getArgument(0), VehicleCategory.DAMAGED, CarCondition.ACCIDENTADO));
        when(carService.getRelatedCars(any())).thenReturn(List.of());
        when(commentService.getCarComments(any())).thenReturn(List.of());
        when(commentService.getCommentCount(any())).thenReturn(0L);
        when(favoriteService.isFavorite(any(), any())).thenReturn(false);
        when(recentlyViewedService.getRecentlyViewedCars(any(), eq(5))).thenReturn(List.of());
        when(carService.createCar(any(), eq(1L))).thenReturn(sampleCar(99L, VehicleCategory.PASSENGER_CAR, CarCondition.OCASION));
        when(carService.findCarsWithFilters(any(), any())).thenReturn(new PageImpl<>(
            List.of(sampleCar(8L, VehicleCategory.DAMAGED, CarCondition.ACCIDENTADO)),
            PageRequest.of(0, 12),
            1
        ));
        when(userService.getUserByEmail(SELLER_EMAIL)).thenReturn(sampleUser(1L, SELLER_EMAIL, UserRole.VENDEDOR));
        when(dealerService.getDealerByUserId(1L)).thenReturn(sampleDealer(1L, SELLER_EMAIL));
        when(dealerService.getDealerSearchOptions()).thenReturn(sampleDealerSearchOptions());
        when(dealerService.getDealerRegions()).thenReturn(List.of("Madrid", "Catalunya", "Andalucia"));
        when(dealerService.getDealerDirectoryEntries(any(), any())).thenReturn(sampleDealerDirectory());
        when(dealerService.getDealerDirectoryByLetter(any(), any())).thenReturn(sampleDealerDirectoryByLetter());
        when(dealerService.getDealerNamesByLetter()).thenReturn(Map.of(
            "A", List.of("Atlas Madrid Motex"),
            "B", List.of("Boreal Barcelona Desguace")
        ));
        when(dealerService.getDealerById(1L)).thenReturn(sampleDealer(1L, "automax@example.com"));
        when(dealerService.buildDirectoryEntry(any())).thenReturn(sampleDealerDirectory().get(0));
        when(carService.getCarsByDealer(1L)).thenReturn(List.of(sampleCar(21L, VehicleCategory.DAMAGED, CarCondition.ACCIDENTADO)));
    }

    @Test
    void vehicleListingCreationWithTwentyImagesAndCategory() throws Exception {
        MockMultipartHttpServletRequestBuilder builder = multipart("/dashboard/listings/add");
        builder.param("brand", "Toyota");
        builder.param("model", "Corolla");
        builder.param("year", "2024");
        builder.param("price", "22000");
        builder.param("mileage", "1200");
        builder.param("fuelType", "GASOLINA");
        builder.param("transmission", "AUTOMATICO");
        builder.param("condition", "OCASION");
        builder.param("category", "PASSENGER_CAR");
        builder.param("locale", "es");

        for (int i = 0; i < 20; i++) {
            builder.file(new MockMultipartFile("imageFiles", "image-" + i + ".jpg", "image/jpeg", ("image-" + i).getBytes()));
        }

        mockMvc.perform(builder.principal(new UsernamePasswordAuthenticationToken(
                SELLER_EMAIL,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"))
            )))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard/listings"));

        verify(carService, times(1)).createCar(any(), eq(1L));
    }

    @Test
    void multiLanguageSwitchingAcrossPages() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/").param("lang", "en").session(session))
            .andExpect(status().isOk());
        assertEquals("en", session.getAttribute(LocaleInterceptor.SESSION_LOCALE_KEY));

        mockMvc.perform(get("/contact").session(session))
            .andExpect(status().isOk());
        assertEquals("en", session.getAttribute(LocaleInterceptor.SESSION_LOCALE_KEY));
    }

    @Test
    void recentlyViewedTrackingAcrossMultipleVehicles() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/cars/1").session(session))
            .andExpect(status().isOk());
        mockMvc.perform(get("/cars/2").session(session))
            .andExpect(status().isOk());

        verify(recentlyViewedService).addToRecentlyViewed(eq(1L), any(HttpSession.class), eq(null), any());
        verify(recentlyViewedService).addToRecentlyViewed(eq(2L), any(HttpSession.class), eq(null), any());
    }

    @Test
    void searchWithCategoryAndConditionFilters() throws Exception {
        mockMvc.perform(get("/cars")
                .param("categories", "DAMAGED")
                .param("conditions", "ACCIDENTADO"))
            .andExpect(status().isOk());

        ArgumentCaptor<CarFilterDTO> captor = ArgumentCaptor.forClass(CarFilterDTO.class);
        verify(carService).findCarsWithFilters(captor.capture(), any());
        CarFilterDTO filters = captor.getValue();
        assertEquals(List.of(VehicleCategory.DAMAGED), filters.getCategories());
        assertEquals(List.of(CarCondition.ACCIDENTADO), filters.getConditions());
        assertTrue(filters.getCategories().contains(VehicleCategory.DAMAGED));
    }

    @Test
    void advancedSearchBindsBrandModelYearFuelAndTransmission() throws Exception {
        mockMvc.perform(get("/cars")
                .param("brands", "Audi")
                .param("model", "A3")
                .param("yearFrom", "1936")
                .param("yearTo", "2026")
                .param("fuelTypes", "GASOLINA", "ELECTRICO")
                .param("transmissions", "MANUAL", "AUTOMATICO"))
            .andExpect(status().isOk());

        ArgumentCaptor<CarFilterDTO> captor = ArgumentCaptor.forClass(CarFilterDTO.class);
        verify(carService, times(1)).findCarsWithFilters(captor.capture(), any());
        CarFilterDTO filters = captor.getValue();
        assertEquals(List.of("Audi"), filters.getBrands());
        assertEquals("A3", filters.getModel());
        assertEquals(1936, filters.getYearFrom());
        assertEquals(2026, filters.getYearTo());
        assertEquals(List.of("GASOLINA", "ELECTRICO"), filters.getFuelTypes());
        assertEquals(List.of("MANUAL", "AUTOMATICO"), filters.getTransmissions());
    }

    @Test
    void invalidModelForSelectedBrandIsIgnored() throws Exception {
        mockMvc.perform(get("/cars")
                .param("brands", "Audi")
                .param("model", "Corolla"))
            .andExpect(status().isOk());

        ArgumentCaptor<CarFilterDTO> captor = ArgumentCaptor.forClass(CarFilterDTO.class);
        verify(carService, times(1)).findCarsWithFilters(captor.capture(), any());
        CarFilterDTO filters = captor.getValue();
        assertEquals(List.of("Audi"), filters.getBrands());
        assertEquals(null, filters.getModel());
    }

    @Test
    void expandableAdvancedFiltersBindSupportedCriteria() throws Exception {
        mockMvc.perform(get("/cars")
                .param("minPrice", "1000")
                .param("maxPrice", "300000")
                .param("color", "AZUL")
                .param("colorCode", "BG1")
                .param("bodyTypes", "SUV", "COMMERCIAL_VEHICLE")
                .param("refinedFuelType", "YES")
                .param("minMileage", "10000")
                .param("maxMileage", "200000")
                .param("origins", "GERMANY", "JAPAN")
                .param("nearbyRadiusKm", "50")
                .param("referenceLatitude", "40.4168")
                .param("referenceLongitude", "-3.7038")
                .param("registrationAvailable", "NO")
                .param("awaitingVerification", "true")
                .param("fullInstructionBooklet", "YES")
                .param("allKeysAvailable", "NO")
                .param("engineDamage", "YES")
                .param("lowerDamage", "NO")
                .param("drivable", "YES")
                .param("movable", "NO")
                .param("engineRuns", "YES")
                .param("airbagsIntact", "NO"))
            .andExpect(status().isOk());

        ArgumentCaptor<CarFilterDTO> captor = ArgumentCaptor.forClass(CarFilterDTO.class);
        verify(carService, times(1)).findCarsWithFilters(captor.capture(), any());
        CarFilterDTO filters = captor.getValue();
        assertEquals(BigDecimal.valueOf(1000), filters.getMinPrice());
        assertEquals(BigDecimal.valueOf(300000), filters.getMaxPrice());
        assertEquals("AZUL", filters.getColor());
        assertEquals("BG1", filters.getColorCode());
        assertEquals(List.of("SUV", "COMMERCIAL_VEHICLE"), filters.getBodyTypes());
        assertEquals(TriStateOption.YES, filters.getRefinedFuelType());
        assertEquals(10000, filters.getMinMileage());
        assertEquals(200000, filters.getMaxMileage());
        assertEquals(List.of("GERMANY", "JAPAN"), filters.getOrigins());
        assertEquals(50, filters.getNearbyRadiusKm());
        assertEquals(40.4168, filters.getReferenceLatitude());
        assertEquals(-3.7038, filters.getReferenceLongitude());
        assertEquals(TriStateOption.NO, filters.getRegistrationAvailable());
        assertTrue(filters.getAwaitingVerification());
        assertEquals(TriStateOption.YES, filters.getFullInstructionBooklet());
        assertEquals(TriStateOption.NO, filters.getAllKeysAvailable());
        assertEquals(TriStateOption.YES, filters.getEngineDamage());
        assertEquals(TriStateOption.NO, filters.getLowerDamage());
        assertEquals(TriStateOption.YES, filters.getDrivable());
        assertEquals(TriStateOption.NO, filters.getMovable());
        assertEquals(TriStateOption.YES, filters.getEngineRuns());
        assertEquals(TriStateOption.NO, filters.getAirbagsIntact());
    }

    @Test
    void advancedFiltersNormalizeInvalidValuesAndPreserveViewSwitchLinks() throws Exception {
        mockMvc.perform(get("/cars")
                .param("minPrice", "-1")
                .param("maxPrice", "-2")
                .param("color", "NOT_A_COLOR")
                .param("colorCode", "INVALID")
                .param("bodyTypes", "INVALID", "SUV")
                .param("origins", "INVALID", "JAPAN")
                .param("nearbyRadiusKm", "13"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("href=\"/cars/list?minPrice=0&amp;maxPrice=0&amp;bodyTypes=SUV&amp;origins=JAPAN\"")));

        ArgumentCaptor<CarFilterDTO> captor = ArgumentCaptor.forClass(CarFilterDTO.class);
        verify(carService, times(1)).findCarsWithFilters(captor.capture(), any());
        CarFilterDTO filters = captor.getValue();
        assertEquals(BigDecimal.ZERO, filters.getMinPrice());
        assertEquals(BigDecimal.ZERO, filters.getMaxPrice());
        assertEquals(null, filters.getColor());
        assertEquals(null, filters.getColorCode());
        assertEquals(List.of("SUV"), filters.getBodyTypes());
        assertEquals(List.of("JAPAN"), filters.getOrigins());
        assertEquals(null, filters.getNearbyRadiusKm());
    }

    @Test
    void searchControlsExposeExplicitButtonsAndBrandSelectedState() throws Exception {
        mockMvc.perform(get("/cars")
                .param("brands", "Audi"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Show advanced filters")))
            .andExpect(content().string(containsString(">Close<")))
            .andExpect(content().string(containsString("inventory-brand-group")))
            .andExpect(content().string(containsString("has-selected-brand")))
            .andExpect(content().string(containsString("Selected")))
            .andExpect(content().string(containsString("data-bs-dismiss=\"offcanvas\"")));
    }

    @Test
    void resultsListingShowsEnrichedVehicleSummaryAndExportPrice() throws Exception {
        when(recentlyViewedService.getRecentlyViewedCars(any(), eq(5))).thenReturn(List.of(sampleCar(11L, VehicleCategory.SALVAGE, CarCondition.NUEVO)));

        mockMvc.perform(get("/cars"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Focus Titanium")))
            .andExpect(content().string(containsString("150 HP")))
            .andExpect(content().string(containsString("inventory-export-price")))
            .andExpect(content().string(containsString("inventory-recently-viewed-card")))
            .andExpect(content().string(containsString("inventory-result-card")));
    }

    @Test
    void listViewShowsNewBadgeAndRichSummary() throws Exception {
        when(carService.findCarsWithFilters(any(), any())).thenReturn(new PageImpl<>(
            List.of(sampleCar(12L, VehicleCategory.PASSENGER_CAR, CarCondition.NUEVO)),
            PageRequest.of(0, 12),
            1
        ));

        mockMvc.perform(get("/cars/list"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("150 HP")))
            .andExpect(content().string(containsString("inventory-export-price")))
            .andExpect(content().string(containsString("inventory-result-summary")));
    }

    @Test
    void homepageExposesExpandedVehicleCategoriesAndDamagedByMakeLinks() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/cars?categories=PASSENGER_CAR")))
            .andExpect(content().string(containsString("/cars?categories=COMMERCIAL_VEHICLE")))
            .andExpect(content().string(containsString("/cars?categories=MOTORCYCLE")))
            .andExpect(content().string(containsString("/cars?categories=CAMPER")))
            .andExpect(content().string(containsString("/cars?categories=TRUCK")))
            .andExpect(content().string(containsString("/cars?categories=CARAVAN")))
            .andExpect(content().string(containsString("/cars?categories=TRAILER")))
            .andExpect(content().string(containsString("/cars?categories=BUS")))
            .andExpect(content().string(containsString("/cars?categories=OTHER")))
            .andExpect(content().string(containsString("/cars?brands=Audi&amp;categories=DAMAGED")));
    }

    @Test
    void categoryRouteAcceptsExpandedVehicleCategory() throws Exception {
        mockMvc.perform(get("/cars/category/MOTORCYCLE"))
            .andExpect(status().isOk());

        ArgumentCaptor<CarFilterDTO> captor = ArgumentCaptor.forClass(CarFilterDTO.class);
        verify(carService, times(1)).findCarsWithFilters(captor.capture(), any());
        assertEquals(List.of(VehicleCategory.MOTORCYCLE), captor.getValue().getCategories());
    }

    @Test
    void mainAndSecondaryNavigationExposeApprovedRoutes() throws Exception {
        mockMvc.perform(get("/terms"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("href=\"/cars/damaged\"")))
            .andExpect(content().string(containsString("href=\"/cars/salvage\"")))
            .andExpect(content().string(containsString("href=\"/cars?categories=PASSENGER_CAR&amp;conditions=OCASION\"")))
            .andExpect(content().string(containsString("href=\"/dealers\"")))
            .andExpect(content().string(containsString("href=\"/terms\"")))
            .andExpect(content().string(containsString("href=\"/disclaimer\"")))
            .andExpect(content().string(containsString("href=\"/privacy\"")))
            .andExpect(content().string(containsString("href=\"/faq\"")))
            .andExpect(content().string(containsString("href=\"/parts-order-status\"")))
            .andExpect(content().string(containsString("href=\"/quality-codes\"")))
            .andExpect(content().string(not(containsString("href=\"/coming-soon?feature=used-parts\""))));
    }

    @Test
    void legalAndSupportPagesArePubliclyReachable() throws Exception {
        mockMvc.perform(get("/disclaimer")).andExpect(status().isOk());
        mockMvc.perform(get("/privacy")).andExpect(status().isOk());
        mockMvc.perform(get("/faq")).andExpect(status().isOk());
        mockMvc.perform(get("/parts-order-status")).andExpect(status().isOk());
        mockMvc.perform(get("/quality-codes")).andExpect(status().isOk());
    }

    @Test
    void homepageExposesSpanishDealerSearchAndDirectoryCount() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Filtra por empresa o region espanola")))
            .andExpect(content().string(containsString("Atlas Madrid Motex")))
            .andExpect(content().string(containsString("distribuidores disponibles")));
    }

    @Test
    void dealerDirectoryShowsSearchRegionAndAlphabeticalGrouping() throws Exception {
        mockMvc.perform(get("/dealers").param("query", "Atlas").param("region", "Madrid"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Distribuidor")))
            .andExpect(content().string(containsString("Todas las regiones")))
            .andExpect(content().string(containsString("Indice alfabetico")))
            .andExpect(content().string(containsString("Atlas Madrid Motex")))
            .andExpect(content().string(containsString("Madrid")));
    }

    @Test
    void dealerDetailShowsSpecializationAndRegion() throws Exception {
        mockMvc.perform(get("/dealers/1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Motex")))
            .andExpect(content().string(containsString("Madrid")))
            .andExpect(content().string(containsString("Vehículos de este Concesionario")));
    }

    private Car sampleCar(Long id, VehicleCategory category, CarCondition condition) {
        Dealer dealer = sampleDealer(3L, "dealer@example.com");

        Car car = new Car();
        car.setId(id);
        car.setMake("Ford");
        car.setModel("Focus");
        car.setVariant("Titanium");
        car.setYear(2022);
        car.setPrice(BigDecimal.valueOf(17500));
        car.setExportPrice(BigDecimal.valueOf(16100));
        car.setMileage(45000);
        car.setColor("Blue");
        car.setPowerHp(150);
        car.setCategory(category);
        car.setCondition(condition);
        car.setLocale("es");
        car.setDescription("nap, cámara, keyless entry y mantenimiento al día para una compra clara y rápida.");
        car.setImages(List.of("car-" + id + ".jpg"));
        car.setDealer(dealer);
        return car;
    }

    private Dealer sampleDealer(Long id, String email) {
        Dealer dealer = new Dealer();
        dealer.setId(id);
        dealer.setName("AutoMax Madrid");
        dealer.setEmail(email);
        dealer.setPhone("+34123456789");
        dealer.setCity("Madrid");
        dealer.setDescription("Concesionario especializado en vehiculo de ocasion y Motex profesional.");
        return dealer;
    }

    private List<String> sampleDealerSearchOptions() {
        return List.of("Atlas Madrid Motex", "Boreal Barcelona Desguace", "Delta Sevilla Export");
    }

    private List<DealerDirectoryEntry> sampleDealerDirectory() {
        return List.of(
            DealerDirectoryEntry.builder()
                .dealerId(1L)
                .companyName("Atlas Madrid Motex")
                .specialization("Motex")
                .region("Madrid")
                .city("Madrid")
                .email("atlas@example.com")
                .phone("+34911111222")
                .listingCount(18)
                .realDealer(true)
                .build(),
            DealerDirectoryEntry.builder()
                .companyName("Boreal Barcelona Desguace")
                .specialization("Desguace")
                .region("Catalunya")
                .city("Barcelona")
                .email("boreal@example.com")
                .phone("+34933334444")
                .listingCount(14)
                .realDealer(false)
                .build()
        );
    }

    private Map<String, List<DealerDirectoryEntry>> sampleDealerDirectoryByLetter() {
        return Map.of(
            "A", List.of(sampleDealerDirectory().get(0)),
            "B", List.of(sampleDealerDirectory().get(1))
        );
    }

    private User sampleUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}

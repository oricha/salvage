package com.cardealer.integration;

import com.cardealer.config.LocaleConfig;
import com.cardealer.config.LocaleInterceptor;
import com.cardealer.config.WebConfig;
import com.cardealer.controller.CarController;
import com.cardealer.controller.ContactController;
import com.cardealer.controller.DashboardController;
import com.cardealer.controller.GlobalModelAttributesController;
import com.cardealer.controller.HomeController;
import com.cardealer.dto.CarFilterDTO;
import com.cardealer.model.Car;
import com.cardealer.model.Dealer;
import com.cardealer.model.enums.CarCondition;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
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

@WebMvcTest(controllers = {DashboardController.class, HomeController.class, CarController.class, ContactController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalModelAttributesController.class, LocaleConfig.class, LocaleInterceptor.class, WebConfig.class})
class PortalMarketplaceIntegrationTest {

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

        mockMvc.perform(builder)
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

    private Car sampleCar(Long id, VehicleCategory category, CarCondition condition) {
        Dealer dealer = new Dealer();
        dealer.setId(3L);
        dealer.setName("Dealer");
        dealer.setEmail("dealer@example.com");
        dealer.setPhone("+34123456789");

        Car car = new Car();
        car.setId(id);
        car.setMake("Ford");
        car.setModel("Focus");
        car.setYear(2022);
        car.setPrice(BigDecimal.valueOf(17500));
        car.setMileage(45000);
        car.setColor("Blue");
        car.setCategory(category);
        car.setCondition(condition);
        car.setLocale("es");
        car.setImages(List.of("car-" + id + ".jpg"));
        car.setDealer(dealer);
        return car;
    }
}

package com.cardealer.security;

import com.cardealer.config.CustomUserDetailsService;
import com.cardealer.config.RoleBasedAuthenticationSuccessHandler;
import com.cardealer.controller.DashboardController;
import com.cardealer.controller.FavoriteController;
import com.cardealer.controller.MessageController;
import com.cardealer.controller.UserController;
import com.cardealer.dto.DashboardStats;
import com.cardealer.dto.MarketplaceKpiSnapshot;
import com.cardealer.model.Dealer;
import com.cardealer.model.User;
import com.cardealer.model.enums.UserRole;
import com.cardealer.repository.UserRepository;
import com.cardealer.service.CarService;
import com.cardealer.service.DealerService;
import com.cardealer.service.FavoriteService;
import com.cardealer.service.LocalizationService;
import com.cardealer.service.MarketplaceMetricsService;
import com.cardealer.service.MessageService;
import com.cardealer.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = {
        UserController.class,
        DashboardController.class,
        FavoriteController.class,
        MessageController.class
    },
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.cardealer.config.WebConfig.class,
            com.cardealer.config.LocaleConfig.class,
            com.cardealer.config.LocaleInterceptor.class,
            com.cardealer.controller.GlobalModelAttributesController.class
        }
    )
)
@AutoConfigureMockMvc
@Import({
    CustomUserDetailsService.class,
    RoleBasedAuthenticationSuccessHandler.class,
    ApplicationLoginSecurityTest.TestSecurityConfiguration.class
})
class ApplicationLoginSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private DealerService dealerService;

    @MockBean
    private CarService carService;

    @MockBean
    private MessageService messageService;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private LocalizationService localizationService;
    @MockBean
    private MarketplaceMetricsService marketplaceMetricsService;

    @BeforeEach
    void setUp() {
        when(userService.getUserByEmail("seller@example.com")).thenReturn(sampleUser(1L, "seller@example.com", UserRole.VENDEDOR, true));
        when(userService.getUserByEmail("buyer@example.com")).thenReturn(sampleUser(2L, "buyer@example.com", UserRole.COMPRADOR, true));
        when(dealerService.getDealerByUserId(1L)).thenReturn(sampleDealer(10L, "seller@example.com"));
        when(carService.getDealerStats(10L)).thenReturn(new DashboardStats(0L, 0L, 0L, List.of(), Map.of()));
        when(marketplaceMetricsService.getMarketplaceKpis()).thenReturn(
            new MarketplaceKpiSnapshot(0L, 0L, 0L, 0L, 0L, java.math.BigDecimal.ZERO)
        );
    }

    @Test
    void successfulSellerLoginRedirectsToDashboard() throws Exception {
        when(userRepository.findByEmail("seller@example.com"))
            .thenReturn(Optional.of(persistedUser(1L, "seller@example.com", "secret123", UserRole.VENDEDOR, true)));

        mockMvc.perform(formLogin("/login").user("seller@example.com").password("secret123"))
            .andExpect(authenticated().withUsername("seller@example.com"))
            .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void successfulBuyerLoginRedirectsToProfile() throws Exception {
        when(userRepository.findByEmail("buyer@example.com"))
            .thenReturn(Optional.of(persistedUser(2L, "buyer@example.com", "secret123", UserRole.COMPRADOR, true)));

        mockMvc.perform(formLogin("/login").user("buyer@example.com").password("secret123"))
            .andExpect(authenticated().withUsername("buyer@example.com"))
            .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void invalidCredentialsRedirectBackToLogin() throws Exception {
        when(userRepository.findByEmail("seller@example.com"))
            .thenReturn(Optional.of(persistedUser(1L, "seller@example.com", "secret123", UserRole.VENDEDOR, true)));

        mockMvc.perform(formLogin("/login").user("seller@example.com").password("wrong-password"))
            .andExpect(unauthenticated())
            .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void rememberMeAddsCookieOnSuccessfulLogin() throws Exception {
        when(userRepository.findByEmail("seller@example.com"))
            .thenReturn(Optional.of(persistedUser(1L, "seller@example.com", "secret123", UserRole.VENDEDOR, true)));

        mockMvc.perform(post("/login")
                .param("username", "seller@example.com")
                .param("password", "secret123")
                .param("remember-me", "on"))
            .andExpect(authenticated())
            .andExpect(cookie().exists("remember-me"));
    }

    @Test
    void logoutRedirectsToLoginWithFeedbackFlag() throws Exception {
        mockMvc.perform(post("/logout").with(user("seller@example.com").roles("VENDEDOR")))
            .andExpect(redirectedUrl("/login?logout=true"))
            .andExpect(unauthenticated());
    }

    @Test
    void anonymousUserIsRedirectedFromProtectedProfilePage() throws Exception {
        mockMvc.perform(get("/profile"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "seller@example.com", roles = "VENDEDOR")
    void sellerCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "buyer@example.com", roles = "COMPRADOR")
    void buyerCannotAccessDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "buyer@example.com", roles = "COMPRADOR")
    void authenticatedUsersAreRedirectedAwayFromLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/profile"));
    }

    private User persistedUser(Long id, String email, String rawPassword, UserRole role, boolean enabled) {
        User user = sampleUser(id, email, role, enabled);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return user;
    }

    private User sampleUser(Long id, String email, UserRole role, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }

    private Dealer sampleDealer(Long id, String email) {
        Dealer dealer = new Dealer();
        dealer.setId(id);
        dealer.setName("Dealer");
        dealer.setEmail(email);
        dealer.setPhone("+34123456789");
        return dealer;
    }

    @TestConfiguration
    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                                UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder,
                                                AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(userDetailsService);
            provider.setPasswordEncoder(passwordEncoder);

            http
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/register", "/forgot-password").permitAll()
                    .requestMatchers("/dashboard/**").hasRole("VENDEDOR")
                    .requestMatchers("/profile", "/profile/**", "/favorites/**", "/messages/**").authenticated()
                    .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(authenticationSuccessHandler)
                    .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .deleteCookies("JSESSIONID", "remember-me")
                )
                .rememberMe(remember -> remember
                    .key("uniqueAndSecretCarDealerKey2024")
                    .userDetailsService(userDetailsService)
                    .rememberMeParameter("remember-me")
                    .tokenValiditySeconds(86400)
                )
                .sessionManagement(session -> session
                    .sessionFixation().migrateSession()
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
                );

            return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        }
    }
}

package com.cardealer.property;

import com.cardealer.config.LocaleInterceptor;
import com.cardealer.model.LocalizedContent;
import com.cardealer.repository.LocalizedContentRepository;
import com.cardealer.service.LocalizationService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Tag;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiLanguagePropertiesTest {

    private static final List<Locale> SUPPORTED = List.of(
        Locale.ENGLISH,
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("nl"),
        Locale.GERMAN,
        Locale.FRENCH
    );

    @Property
    @Tag("Feature: portal-venta-coches, Property 12: Multi-Language Content Completeness")
    void multiLanguageContentCompleteness(@ForAll("messageKeys") String key) {
        StaticMessageSource messageSource = new StaticMessageSource();
        for (Locale locale : SUPPORTED) {
            messageSource.addMessage(key, locale, key + "-" + locale.getLanguage());
        }

        LocalizedContentRepository repository = mock(LocalizedContentRepository.class);
        when(repository.findByContentKeyAndLocale(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(Optional.empty());

        LocalizationService service = new LocalizationService(messageSource, repository);

        for (Locale locale : SUPPORTED) {
            String resolved = service.getMessage(key, locale);
            assertNotEquals(key, resolved);
            assertEquals(key + "-" + locale.getLanguage(), resolved);
        }
    }

    @Property
    @Tag("Feature: portal-venta-coches, Property 13: Language Preference Persistence")
    void languagePreferencePersistence(@ForAll("languages") String requestedLanguage,
                                       @ForAll("languages") String browserLanguage) {
        LocaleResolver resolver = new CookieLocaleResolver("user-locale");
        LocaleInterceptor interceptor = new LocaleInterceptor(resolver);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("GET");
        request.setRequestURI("/cars");
        request.addPreferredLocale(Locale.forLanguageTag(browserLanguage));

        if (requestedLanguage != null) {
            request.setParameter("lang", requestedLanguage);
        }

        interceptor.preHandle(request, response, new Object());

        String expected = requestedLanguage != null
            ? normalize(requestedLanguage)
            : normalize(browserLanguage);

        assertEquals(expected, request.getSession().getAttribute(LocaleInterceptor.SESSION_LOCALE_KEY));
        assertEquals(expected, resolver.resolveLocale(request).toLanguageTag());
    }

    @net.jqwik.api.Provide
    net.jqwik.api.Arbitrary<String> languages() {
        return net.jqwik.api.Arbitraries.of("en", "es", "nl", "de", "fr", "it");
    }

    @net.jqwik.api.Provide
    net.jqwik.api.Arbitrary<String> messageKeys() {
        return net.jqwik.api.Arbitraries.of("nav.home", "nav.inventory", "common.language", "button.search");
    }

    private String normalize(String language) {
        Locale candidate = Locale.forLanguageTag(language);
        return SUPPORTED.stream()
            .filter(locale -> locale.getLanguage().equalsIgnoreCase(candidate.getLanguage()))
            .findFirst()
            .orElse(Locale.ENGLISH)
            .toLanguageTag();
    }
}

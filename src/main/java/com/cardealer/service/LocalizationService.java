package com.cardealer.service;

import com.cardealer.config.LocaleConfig;
import com.cardealer.exception.LocalizationException;
import com.cardealer.model.LocalizedContent;
import com.cardealer.repository.LocalizedContentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocalizationService {

    private static final List<Locale> SUPPORTED_LOCALES = List.of(
        Locale.ENGLISH,
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("nl"),
        Locale.GERMAN,
        Locale.FRENCH
    );

    private final MessageSource messageSource;
    private final LocalizedContentRepository localizedContentRepository;

    @Cacheable("i18n")
    public String getMessage(String key, Locale locale) {
        try {
            Locale effectiveLocale = normalizeLocale(locale);

            Optional<LocalizedContent> customContent = localizedContentRepository
                .findByContentKeyAndLocale(key, effectiveLocale.getLanguage());

            if (customContent.isPresent()) {
                return customContent.get().getContent();
            }

            String localizedMessage = messageSource.getMessage(key, null, key, effectiveLocale);
            if (!key.equals(localizedMessage)) {
                return localizedMessage;
            }

            return messageSource.getMessage(key, null, key, Locale.ENGLISH);
        } catch (Exception ex) {
            throw new LocalizationException("No se pudo resolver el contenido localizado para la clave: " + key, ex);
        }
    }

    public List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    public Locale resolveLocale(HttpServletRequest request) {
        Locale requestLocale = request != null ? request.getLocale() : null;
        return normalizeLocale(requestLocale);
    }

    private Locale normalizeLocale(Locale locale) {
        if (locale == null) {
            return LocaleConfig.DEFAULT_LOCALE;
        }

        return SUPPORTED_LOCALES.stream()
            .filter(supported -> supported.getLanguage().equalsIgnoreCase(locale.getLanguage()))
            .findFirst()
            .orElse(Locale.ENGLISH);
    }
}

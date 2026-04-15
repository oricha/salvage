package com.cardealer.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleInterceptor implements HandlerInterceptor {

    public static final String SESSION_LOCALE_KEY = "preferred-locale";

    private static final List<Locale> SUPPORTED_LOCALES = List.of(
        Locale.ENGLISH,
        Locale.forLanguageTag("es"),
        Locale.forLanguageTag("nl"),
        Locale.GERMAN,
        Locale.FRENCH
    );

    private final LocaleResolver localeResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Locale resolvedLocale = extractRequestedLocale(request);
        if (resolvedLocale == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object sessionLocale = session.getAttribute(SESSION_LOCALE_KEY);
                if (sessionLocale instanceof String languageTag) {
                    resolvedLocale = findSupportedLocale(languageTag);
                }
            }
        }

        if (resolvedLocale == null) {
            Locale browserLocale = request.getLocale();
            if (browserLocale != null) {
                resolvedLocale = findSupportedLocale(browserLocale.toLanguageTag());
            }
        }

        if (resolvedLocale == null) {
            resolvedLocale = localeResolver.resolveLocale(request);
        }

        if (resolvedLocale == null) {
            resolvedLocale = LocaleConfig.DEFAULT_LOCALE;
        }

        request.getSession(true).setAttribute(SESSION_LOCALE_KEY, resolvedLocale.toLanguageTag());
        localeResolver.setLocale(request, response, resolvedLocale);
        return true;
    }

    private Locale extractRequestedLocale(HttpServletRequest request) {
        String lang = request.getParameter("lang");
        if (lang == null || lang.isBlank()) {
            return null;
        }
        return findSupportedLocale(lang);
    }

    private Locale findSupportedLocale(String languageTag) {
        Locale candidate = Locale.forLanguageTag(languageTag);
        return SUPPORTED_LOCALES.stream()
            .filter(locale -> locale.getLanguage().equalsIgnoreCase(candidate.getLanguage()))
            .findFirst()
            .orElse(Locale.ENGLISH);
    }
}

package com.cardealer.controller;

import com.cardealer.model.User;
import com.cardealer.service.LocalizationService;
import com.cardealer.service.MessageService;
import com.cardealer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Locale;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributesController {

    private final UserService userService;
    private final MessageService messageService;
    private final LocalizationService localizationService;

    @ModelAttribute("unreadMessageCount")
    public long unreadMessageCount(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return 0L;
        }

        try {
            User user = userService.getUserByEmail(authentication.getName());
            return messageService.getUnreadCount(user.getId());
        } catch (Exception ignored) {
            return 0L;
        }
    }

    @ModelAttribute("supportedLocales")
    public List<Locale> supportedLocales() {
        return localizationService.getSupportedLocales();
    }

    @ModelAttribute("currentLocale")
    public Locale currentLocale() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Locale.forLanguageTag("es");
        }
        return localizationService.resolveLocale(attributes.getRequest());
    }
}

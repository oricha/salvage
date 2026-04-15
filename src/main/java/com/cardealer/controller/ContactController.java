package com.cardealer.controller;

import com.cardealer.dto.ContactFormDTO;
import com.cardealer.dto.ContactInteractionRequest;
import com.cardealer.model.User;
import com.cardealer.model.enums.InteractionType;
import com.cardealer.service.ContactService;
import com.cardealer.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final UserService userService;

    @PostMapping("/contact")
    public String submitContactForm(
            @Valid @ModelAttribute("contactForm") ContactFormDTO contactForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contactForm", contactForm);
            return "contact";
        }

        contactService.saveContactForm(contactForm);

        redirectAttributes.addFlashAttribute("successMessage",
            "Hemos recibido tu mensaje. Te responderemos lo antes posible.");

        return "redirect:/contact";
    }

    @PostMapping("/api/contact/log")
    @ResponseBody
    public ResponseEntity<Void> logContactInteraction(@Valid @RequestBody ContactInteractionRequest requestBody,
                                                      Authentication authentication,
                                                      HttpServletRequest request) {
        User user = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            user = userService.getUserByEmail(authentication.getName());
        }

        contactService.logInteraction(
            requestBody.getCarId(),
            InteractionType.valueOf(requestBody.getInteractionType().toUpperCase()),
            request,
            user
        );
        return ResponseEntity.ok().build();
    }
}

package com.cardealer.controller;

import com.cardealer.dto.ContactFormDTO;
import com.cardealer.model.enums.BodyType;
import com.cardealer.service.CarService;
import com.cardealer.service.SEOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CarService carService;
    private final SEOService seoService;

    @GetMapping("/")
    public String home(Model model) {
        // Load latest 8 cars
        model.addAttribute("latestCars", carService.getLatestCars());
        model.addAttribute("totalCars", carService.getTotalCarCount());
        model.addAttribute("availableBrands", carService.getAvailableBrands());
        
        // Load body type categories
        List<BodyType> bodyTypes = Arrays.asList(BodyType.values());
        model.addAttribute("bodyTypes", bodyTypes);
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generatePageMetadata("home", Locale.forLanguageTag("es")),
            "/"
        ));
        
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageDescription", "Conoce la misión del portal y cómo ayudamos a compradores y vendedores.");
        model.addAttribute("pageKeywords", "acerca de, portal coches, empresa");
        model.addAttribute("ogTitle", "Acerca de Portal de Coches");
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        if (!model.containsAttribute("contactForm")) {
            model.addAttribute("contactForm", new ContactFormDTO());
        }
        model.addAttribute("pageDescription", "Ponte en contacto con Portal de Coches para resolver dudas sobre vehículos o listados.");
        model.addAttribute("pageKeywords", "contacto, soporte coches, ayuda portal");
        model.addAttribute("ogTitle", "Contacto");
        return "contact";
    }
}

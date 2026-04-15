package com.cardealer.controller;

import com.cardealer.dto.ContactFormDTO;
import com.cardealer.dto.BreadcrumbItem;
import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.service.CarService;
import com.cardealer.service.SEOService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CarService carService;
    private final SEOService seoService;

    @GetMapping("/")
    public String home(Model model, Locale locale) {
        Locale effectiveLocale = locale != null ? locale : Locale.forLanguageTag("es");

        // Load latest 8 cars
        model.addAttribute("latestCars", carService.getLatestCars());
        model.addAttribute("featuredCars", carService.getFeaturedCars());
        model.addAttribute("totalCars", carService.getTotalCarCount());
        model.addAttribute("availableBrands", carService.getAvailableBrands());
        model.addAttribute("featuredVehiclesByCategory", Map.of(
            VehicleCategory.DAMAGED, carService.findCarsByCategory(VehicleCategory.DAMAGED, org.springframework.data.domain.PageRequest.of(0, 4)).getContent(),
            VehicleCategory.SALVAGE, carService.findCarsByCategory(VehicleCategory.SALVAGE, org.springframework.data.domain.PageRequest.of(0, 4)).getContent(),
            VehicleCategory.PASSENGER_CAR, carService.findCarsByCategory(VehicleCategory.PASSENGER_CAR, org.springframework.data.domain.PageRequest.of(0, 4)).getContent()
        ));

        // Load body type categories
        List<BodyType> bodyTypes = Arrays.asList(BodyType.values());
        model.addAttribute("bodyTypes", bodyTypes);
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generatePageMetadata("home", effectiveLocale),
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

    @GetMapping("/coming-soon")
    public String comingSoon(@RequestParam(defaultValue = "used-parts") String feature, Model model, Locale locale) {
        String featureKey = switch (feature) {
            case "occasion-vehicles" -> "feature.occasionVehicles";
            default -> "feature.usedParts";
        };

        model.addAttribute("feature", feature);
        model.addAttribute("featureKey", featureKey);
        model.addAttribute("featureTitle", featureKey);
        model.addAttribute("breadcrumbItems", List.of(
            new BreadcrumbItem("Inicio", "/", false),
            new BreadcrumbItem("Próximamente", null, true)
        ));
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generatePageMetadata("coming-soon-" + feature, locale != null ? locale : Locale.forLanguageTag("es")),
            "/coming-soon?feature=" + feature
        ));
        return "coming-soon";
    }
}

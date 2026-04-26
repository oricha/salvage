package com.cardealer.controller;

import com.cardealer.catalog.AdvancedSearchCatalog;
import com.cardealer.catalog.VehicleCategoryCatalog;
import com.cardealer.dto.ContactFormDTO;
import com.cardealer.dto.BreadcrumbItem;
import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.service.CarService;
import com.cardealer.service.DealerService;
import com.cardealer.service.SEOService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CarService carService;
    private final DealerService dealerService;
    private final SEOService seoService;
    private final MessageSource messageSource;

    @GetMapping("/")
    public String home(Model model, Locale locale) {
        Locale effectiveLocale = locale != null ? locale : Locale.forLanguageTag("es");

        // Load latest 8 cars
        model.addAttribute("latestCars", carService.getLatestCars());
        model.addAttribute("featuredCars", carService.getFeaturedCars());
        model.addAttribute("totalCars", carService.getTotalCarCount());
        model.addAttribute("availableBrands", carService.getAvailableBrands());
        model.addAttribute("featuredHeroTabs", List.of(
            Map.of("label", "Damaged", "description", "Repairable vehicles with full listing data", "url", "/cars?categories=DAMAGED"),
            Map.of("label", "Desguace", "description", "Salvage stock ready for export or dismantling", "url", "/cars?categories=SALVAGE"),
            Map.of("label", "Occasion", "description", "Occasion cars from professional sellers", "url", "/cars?categories=PASSENGER_CAR")
        ));
        model.addAttribute("homepageStats", List.of(
            Map.of("value", "10.000+", "label", "coches dañados disponibles"),
            Map.of("value", "20.000+", "label", "coches de desguace"),
            Map.of("value", "3.000.000", "label", "interacciones de inventario"),
            Map.of("value", "~2.000", "label", "vehiculos de ocasion"),
            Map.of("value", "250+", "label", "empresas participantes")
        ));
        model.addAttribute("homepageValueProps", List.of(
            Map.of(
                "title", "One-Stop-Shop",
                "description", "Encuentra coches dañados, siniestrados y vehiculos de ocasion dentro de un unico marketplace profesional."
            ),
            Map.of(
                "title", "Plataforma unificada",
                "description", "Trabajamos con multiples categorias, distribuidores y regiones para simplificar la busqueda y comparacion."
            ),
            Map.of(
                "title", "Transparencia y calidad",
                "description", "Las fichas y distribuidores se presentan con estructura clara, trazabilidad y foco en calidad profesional."
            )
        ));
        model.addAttribute("primaryVehicleTypes", VehicleCategoryCatalog.primaryCategories());
        model.addAttribute("extraVehicleTypes", VehicleCategoryCatalog.secondaryCategories());
        model.addAttribute("searchBrands", buildSearchBrands());
        model.addAttribute("modelsByMake", buildModelsByMake());
        model.addAttribute("fuelOptions", List.of("Petrol", "Diesel", "LPG", "Electric", "Hybrid", "Plug-in hybrid"));
        model.addAttribute("transmissionOptions", List.of("Automatic", "Manual"));
        model.addAttribute("advancedColors", List.of("Beige", "Black", "Blue", "Brown", "Grey", "Red", "Silver", "White", "Yellow", "Green", "Orange"));
        model.addAttribute("colorCodes", List.of("ALP", "AZR", "BLK", "BLU", "BRZ", "CRM", "GRF", "GRY", "IVR", "LIM", "MRN", "NAV", "ORG", "PEA", "PNK", "PUR", "RED", "SND", "SLV", "TAN", "TEA", "WHT", "YEL", "CHR", "COP"));
        model.addAttribute("bodyTypeOptions", List.of("Sedan", "SUV", "Hatchback", "Coupe", "Cabriolet", "Estate", "Van", "Pickup", "MPV"));
        model.addAttribute("originOptions", List.of("Netherlands", "Belgium", "Germany", "France", "Spain", "Italy", "Austria"));
        model.addAttribute("detailOptions", List.of("Registration papers available", "All keys available", "Complete manual", "Maintenance history", "Service book"));
        model.addAttribute("damageOptions", List.of("Engine damage", "Steering damage", "Airbags intact", "Moving vehicle", "Driveable", "Rear damage", "Front damage", "Side impact"));
        model.addAttribute("dealerSearchOptions", dealerService.getDealerSearchOptions());
        model.addAttribute("dealersByLetter", dealerService.getDealerNamesByLetter());
        model.addAttribute("dealerDirectoryCount", dealerService.getDealerSearchOptions().size());
        model.addAttribute("dealerRegions", dealerService.getDealerRegions());
        model.addAttribute("makesForDamageGrid", AdvancedSearchCatalog.mergedBrands(carService.getAvailableBrands()).stream().limit(36).toList());
        model.addAttribute("extraStylesheets", List.of("/css/home-salvage.css"));
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

    @GetMapping("/terms")
    public String terms(Model model, Locale locale) {
        return renderInfoPage(
            model,
            locale,
            "info.terms.title",
            "info.terms.subtitle",
            "info.terms.lead",
            List.of("info.terms.point1", "info.terms.point2", "info.terms.point3"),
            "/terms"
        );
    }

    @GetMapping("/disclaimer")
    public String disclaimer(Model model, Locale locale) {
        return renderInfoPage(
            model,
            locale,
            "info.disclaimer.title",
            "info.disclaimer.subtitle",
            "info.disclaimer.lead",
            List.of("info.disclaimer.point1", "info.disclaimer.point2", "info.disclaimer.point3"),
            "/disclaimer"
        );
    }

    @GetMapping("/privacy")
    public String privacy(Model model, Locale locale) {
        return renderInfoPage(
            model,
            locale,
            "info.privacy.title",
            "info.privacy.subtitle",
            "info.privacy.lead",
            List.of("info.privacy.point1", "info.privacy.point2", "info.privacy.point3"),
            "/privacy"
        );
    }

    @GetMapping("/faq")
    public String faq(Model model, Locale locale) {
        return renderInfoPage(
            model,
            locale,
            "info.faq.title",
            "info.faq.subtitle",
            "info.faq.lead",
            List.of("info.faq.point1", "info.faq.point2", "info.faq.point3"),
            "/faq"
        );
    }

    @GetMapping("/parts-order-status")
    public String partsOrderStatus() {
        return "redirect:/contact";
    }

    @GetMapping("/quality-codes")
    public String qualityCodes(Model model, Locale locale) {
        return renderInfoPage(
            model,
            locale,
            "info.qualityCodes.title",
            "info.qualityCodes.subtitle",
            "info.qualityCodes.lead",
            List.of("info.qualityCodes.point1", "info.qualityCodes.point2", "info.qualityCodes.point3"),
            "/quality-codes"
        );
    }

    @GetMapping("/coming-soon")
    public String comingSoon(@RequestParam(defaultValue = "occasion-vehicles") String feature, Model model, Locale locale) {
        if ("used-parts".equals(feature)) {
            return "redirect:/contact";
        }
        String featureKey = switch (feature) {
            case "occasion-vehicles" -> "feature.occasionVehicles";
            default -> "feature.occasionVehicles";
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

    private List<String> buildSearchBrands() {
        TreeSet<String> brands = new TreeSet<>();
        brands.addAll(List.of(
            "Audi", "BMW", "Volkswagen", "Mercedes", "Toyota", "Ford", "Opel", "Peugeot",
            "Renault", "Citroën", "Nissan", "Hyundai", "Kia", "Mazda", "Honda", "Volvo",
            "Tesla", "Seat", "Skoda", "Mini", "Jeep", "Land Rover", "Cupra", "Dacia"
        ));
        brands.addAll(carService.getAvailableBrands());
        return new ArrayList<>(brands);
    }

    private Map<String, List<String>> buildModelsByMake() {
        Map<String, List<String>> models = new LinkedHashMap<>();
        models.put("Audi", List.of("A1", "A3", "A4", "A6", "Q2", "Q5", "Q7"));
        models.put("BMW", List.of("1 Series", "3 Series", "5 Series", "X1", "X3", "X5"));
        models.put("Volkswagen", List.of("Golf", "Polo", "Passat", "T-Roc", "Tiguan", "Transporter"));
        models.put("Mercedes", List.of("A-Class", "C-Class", "E-Class", "CLA", "GLA", "Vito"));
        models.put("Toyota", List.of("Aygo", "Yaris", "Corolla", "C-HR", "RAV4", "Hilux"));
        models.put("Ford", List.of("Fiesta", "Focus", "Puma", "Kuga", "Transit", "Ranger"));
        models.put("Opel", List.of("Corsa", "Astra", "Mokka", "Insignia", "Vivaro"));
        models.put("Peugeot", List.of("208", "308", "3008", "5008", "Partner"));
        models.put("Renault", List.of("Clio", "Megane", "Captur", "Kadjar", "Trafic"));
        models.put("Nissan", List.of("Micra", "Qashqai", "Juke", "X-Trail", "Navara"));
        models.put("Tesla", List.of("Model 3", "Model S", "Model X", "Model Y"));
        return models;
    }

    private String renderInfoPage(Model model,
                                  Locale locale,
                                  String titleKey,
                                  String subtitleKey,
                                  String leadKey,
                                  List<String> pointKeys,
                                  String path) {
        Locale effectiveLocale = locale != null ? locale : Locale.forLanguageTag("es");
        String resolvedTitle = messageSource.getMessage(titleKey, null, effectiveLocale);
        String resolvedLead = messageSource.getMessage(leadKey, null, effectiveLocale);
        model.addAttribute("titleKey", titleKey);
        model.addAttribute("subtitleKey", subtitleKey);
        model.addAttribute("leadKey", leadKey);
        model.addAttribute("pointKeys", pointKeys);
        model.addAttribute("pageDescription", resolvedLead);
        model.addAttribute("pageKeywords", "navigation, information, legal, support");
        model.addAttribute("ogTitle", resolvedTitle);
        model.addAllAttributes(seoService.toModelAttributes(
            seoService.generatePageMetadata(path.substring(1), effectiveLocale),
            path
        ));
        return "info-page";
    }
}

package com.cardealer.controller;

import com.cardealer.dto.BreadcrumbItem;
import com.cardealer.dto.DealerDirectoryEntry;
import com.cardealer.model.Car;
import com.cardealer.model.Dealer;
import com.cardealer.service.CarService;
import com.cardealer.service.DealerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/dealers")
@RequiredArgsConstructor
public class DealerController {

    private final DealerService dealerService;
    private final CarService carService;

    /**
     * List all active dealers
     */
    @GetMapping
    public String listDealers(@RequestParam(required = false) String query,
                              @RequestParam(required = false) String region,
                              Model model) {
        log.info("Loading dealers list with query={} region={}", query, region);
        
        try {
            List<DealerDirectoryEntry> dealerDirectory = dealerService.getDealerDirectoryEntries(query, region);
            Map<String, List<DealerDirectoryEntry>> dealersByLetter = dealerService.getDealerDirectoryByLetter(query, region);

            model.addAttribute("dealerDirectory", dealerDirectory);
            model.addAttribute("dealersByLetter", dealersByLetter);
            model.addAttribute("dealerSearchOptions", dealerService.getDealerSearchOptions());
            model.addAttribute("dealerRegions", dealerService.getDealerRegions());
            model.addAttribute("selectedQuery", query);
            model.addAttribute("selectedRegion", region);
            model.addAttribute("pageDescription", "Busca distribuidores profesionales por empresa, especializacion y region en Espana.");
            model.addAttribute("pageKeywords", "distribuidores, agentes, concesionarios, Espana, Motex, desguace");
            model.addAttribute("ogTitle", "Directorio de distribuidores");
            model.addAttribute("extraStylesheets", List.of("/css/dealer-directory.css"));
            
            log.info("Loaded {} directory entries", dealerDirectory.size());
            return "dealer";
            
        } catch (Exception e) {
            log.error("Error loading dealers", e);
            model.addAttribute("error", "Error al cargar los concesionarios: " + e.getMessage());
            model.addAttribute("extraStylesheets", List.of("/css/dealer-directory.css"));
            return "dealer";
        }
    }

    /**
     * Show dealer detail page
     */
    @GetMapping("/{id}")
    public String dealerDetail(@PathVariable Long id, Model model) {
        log.info("Loading dealer detail for id: {}", id);
        
        try {
            // Get dealer
            Dealer dealer = dealerService.getDealerById(id);
            DealerDirectoryEntry dealerProfile = dealerService.buildDirectoryEntry(dealer);
            
            // Get dealer's cars
            List<Car> dealerCars = carService.getCarsByDealer(id);
            
            model.addAttribute("dealer", dealer);
            model.addAttribute("dealerProfile", dealerProfile);
            model.addAttribute("dealerCars", dealerCars);
            model.addAttribute("totalListings", dealerCars.size());
            model.addAttribute("breadcrumbItems", List.of(
                new BreadcrumbItem("Inicio", "/", false),
                new BreadcrumbItem("Concesionarios", "/dealers", false),
                new BreadcrumbItem(dealer.getName(), null, true)
            ));
            model.addAttribute("pageDescription", dealer.getDescription() != null && !dealer.getDescription().isBlank()
                ? dealer.getDescription()
                : "Consulta el perfil y los vehículos activos de " + dealer.getName() + ".");
            model.addAttribute("pageKeywords", String.join(", ",
                List.of("concesionario", dealer.getName(), "coches en venta")));
            model.addAttribute("ogTitle", dealer.getName());
            model.addAttribute("ogImage", dealer.getLogoUrl() != null && !dealer.getLogoUrl().isBlank()
                ? dealerService.resolveMediaPath(dealer.getLogoUrl(), "/img/store/logo.jpg")
                : "/img/store/logo.jpg");
            model.addAttribute("extraStylesheets", List.of("/css/dealer-directory.css"));
            
            log.info("Loaded dealer: {} with {} cars", dealer.getName(), dealerCars.size());
            return "dealer-single";
            
        } catch (Exception e) {
            log.error("Error loading dealer detail", e);
            model.addAttribute("error", "Error al cargar el concesionario: " + e.getMessage());
            return "404";
        }
    }
}

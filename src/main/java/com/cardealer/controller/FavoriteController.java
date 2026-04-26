package com.cardealer.controller;

import com.cardealer.model.Car;
import com.cardealer.model.User;
import com.cardealer.service.FavoriteService;
import com.cardealer.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/favorites")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    /**
     * Add car to favorites (AJAX endpoint)
     */
    @PostMapping("/add/{carId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addFavorite(
            @PathVariable Long carId,
            Authentication authentication) {
        
        log.info("Adding car {} to favorites", carId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            
            // Add to favorites
            favoriteService.addFavorite(user.getId(), carId);
            
            response.put("success", true);
            response.put("message", "Coche añadido a favoritos");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error adding favorite", e);
            response.put("success", false);
            response.put("message", "Error al añadir a favoritos: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Remove car from favorites (AJAX endpoint)
     */
    @PostMapping("/remove/{carId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long carId,
            Authentication authentication) {
        
        log.info("Removing car {} from favorites", carId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get authenticated user
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            
            // Remove from favorites
            favoriteService.removeFavorite(user.getId(), carId);
            
            response.put("success", true);
            response.put("message", "Coche eliminado de favoritos");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error removing favorite", e);
            response.put("success", false);
            response.put("message", "Error al eliminar de favoritos: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Show user's favorites page
     */
    @GetMapping
    public String showFavorites(Model model, Authentication authentication) {
        log.info("Loading favorites page");
        
        try {
            // Get authenticated user
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            
            // Get user's favorite cars
            List<Car> favoriteCars = favoriteService.getUserFavorites(user.getId());
            
            model.addAttribute("user", user);
            model.addAttribute("favoriteCars", favoriteCars);
            model.addAttribute("pageDescription", "Gestiona tu parking de vehículos guardados y revisa tus últimos vistos.");
            model.addAttribute("pageKeywords", "favoritos, parking, últimos vistos, coches guardados");
            model.addAttribute("ogTitle", "Parking de vehículos");
            
            return "profile-favorite";
            
        } catch (Exception e) {
            log.error("Error loading favorites", e);
            model.addAttribute("error", "Error al cargar favoritos: " + e.getMessage());
            return "profile-favorite";
        }
    }
}

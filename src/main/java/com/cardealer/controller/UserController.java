package com.cardealer.controller;

import com.cardealer.dto.UserRegistrationDTO;
import com.cardealer.exception.DuplicateResourceException;
import com.cardealer.model.Dealer;
import com.cardealer.model.User;
import com.cardealer.model.enums.UserRole;
import com.cardealer.service.DealerService;
import com.cardealer.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DealerService dealerService;

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String loginPage(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return redirectForAuthenticatedUser(authentication);
        }
        model.addAttribute("pageDescription", "Acceso para vendedores y distribuidores del marketplace.");
        model.addAttribute("pageKeywords", "login agente, portal distribuidor, vendedores");
        model.addAttribute("ogTitle", "Acceso agente");
        return "login";
    }

    /**
     * Show registration page
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegistrationDTO", new UserRegistrationDTO());
        return "register";
    }

    /**
     * Process registration
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute UserRegistrationDTO registrationDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        log.info("Processing registration for email: {}", registrationDTO.getEmail());
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in registration form");
            model.addAttribute("userRegistrationDTO", registrationDTO);
            return "register";
        }
        
        // Check if passwords match
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            log.warn("Passwords do not match");
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("userRegistrationDTO", registrationDTO);
            return "register";
        }
        
        // Check if email already exists
        if (userService.emailExists(registrationDTO.getEmail())) {
            log.warn("Email already exists: {}", registrationDTO.getEmail());
            model.addAttribute("error", "El email ya está registrado");
            model.addAttribute("userRegistrationDTO", registrationDTO);
            return "register";
        }
        
        try {
            // Register user
            User user = userService.registerUser(registrationDTO);
            
            // If role is VENDEDOR, create dealer
            if ("VENDEDOR".equals(registrationDTO.getRole())) {
                Dealer dealer = new Dealer();
                dealer.setName(registrationDTO.getDealerName());
                dealer.setEmail(registrationDTO.getEmail());
                dealer.setPhone(registrationDTO.getPhone());
                dealer.setAddress(registrationDTO.getDealerAddress());
                dealer.setCity(registrationDTO.getDealerCity());
                dealer.setDescription(registrationDTO.getDealerDescription());
                dealer.setActive(true);
                
                dealerService.createDealer(dealer, user);
                log.info("Dealer created for user: {}", user.getEmail());
            }
            
            log.info("User registered successfully: {}", user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Registro exitoso. Por favor, inicia sesión.");
            return "redirect:/login";
            
        } catch (DuplicateResourceException e) {
            log.error("Duplicate resource error during registration", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userRegistrationDTO", registrationDTO);
            return "register";
        } catch (Exception e) {
            log.error("Error during registration", e);
            model.addAttribute("error", "Error al registrar usuario. Por favor, inténtelo de nuevo.");
            model.addAttribute("userRegistrationDTO", registrationDTO);
            return "register";
        }
    }

    /**
     * Show forgot password page
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    private String redirectForAuthenticatedUser(Authentication authentication) {
        boolean isSeller = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_VENDEDOR"::equals);
        if (isSeller) {
            return "redirect:/dashboard";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return "redirect:/admin";
        }

        return "redirect:/profile";
    }

    /**
     * Show user profile
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            model.addAttribute("user", user);
            
            // If user is a dealer, get dealer info
            if (user.getRole() == UserRole.VENDEDOR) {
                Dealer dealer = dealerService.getDealerByUserId(user.getId());
                model.addAttribute("dealer", dealer);
            }
        }
        return "profile";
    }

    /**
     * Show profile settings
     */
    @GetMapping("/profile/settings")
    public String profileSettings(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            model.addAttribute("user", user);
            
            // If user is a dealer, get dealer info
            if (user.getRole() == UserRole.VENDEDOR) {
                Dealer dealer = dealerService.getDealerByUserId(user.getId());
                model.addAttribute("dealer", dealer);
            }
        }
        return "profile-setting";
    }

    /**
     * Update user profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute User userDetails,
            @ModelAttribute Dealer dealerDetails,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            
            try {
                // Update user profile
                userService.updateProfile(user.getId(), userDetails);
                
                // If user is a dealer, update dealer information
                if (user.getRole() == UserRole.VENDEDOR && dealerDetails != null) {
                    Dealer dealer = dealerService.getDealerByUserId(user.getId());
                    dealerService.updateDealer(dealer.getId(), dealerDetails);
                    log.info("Dealer profile updated for user: {}", email);
                }
                
                redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
                log.info("Profile updated for user: {}", email);
            } catch (Exception e) {
                log.error("Error updating profile", e);
                redirectAttributes.addFlashAttribute("error", "Error al actualizar perfil");
            }
        }
        
        return "redirect:/profile/settings";
    }
    
    /**
     * Update password
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @ModelAttribute("oldPassword") String oldPassword,
            @ModelAttribute("newPassword") String newPassword,
            @ModelAttribute("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            
            try {
                // Validate passwords match
                if (!newPassword.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
                    return "redirect:/profile/settings";
                }
                
                // Validate password length
                if (newPassword.length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                    return "redirect:/profile/settings";
                }
                
                userService.updatePassword(user.getId(), newPassword);
                redirectAttributes.addFlashAttribute("success", "Contraseña actualizada exitosamente");
                log.info("Password updated for user: {}", email);
            } catch (Exception e) {
                log.error("Error updating password", e);
                redirectAttributes.addFlashAttribute("error", "Error al actualizar contraseña");
            }
        }
        
        return "redirect:/profile/settings";
    }
}

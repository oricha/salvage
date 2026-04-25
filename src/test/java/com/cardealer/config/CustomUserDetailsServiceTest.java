package com.cardealer.config;

import com.cardealer.model.User;
import com.cardealer.model.enums.UserRole;
import com.cardealer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadsUserByEmailAndMapsAuthorities() {
        User user = buildUser("dealer@example.com", UserRole.VENDEDOR, true);
        when(userRepository.findByEmail("dealer@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("dealer@example.com");

        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_VENDEDOR".equals(authority.getAuthority())));
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void marksDisabledUsersAsDisabledInUserDetails() {
        User user = buildUser("buyer@example.com", UserRole.COMPRADOR, false);
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("buyer@example.com");

        assertFalse(userDetails.isEnabled());
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("missing@example.com"));
    }

    private User buildUser(String email, UserRole role, boolean enabled) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }
}

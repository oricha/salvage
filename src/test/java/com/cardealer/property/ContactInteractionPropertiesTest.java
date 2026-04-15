package com.cardealer.property;

import com.cardealer.model.Car;
import com.cardealer.model.ContactInteraction;
import com.cardealer.model.Dealer;
import com.cardealer.model.User;
import com.cardealer.model.enums.InteractionType;
import com.cardealer.model.enums.UserRole;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.ContactFormRepository;
import com.cardealer.repository.ContactInteractionRepository;
import com.cardealer.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.Tag;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContactInteractionPropertiesTest {

    @Property
    @Tag("Feature: portal-venta-coches, Property 10: Contact Interaction Logging")
    void contactInteractionLogging(@ForAll("interactionTypes") InteractionType interactionType,
                                   @ForAll("ipAddresses") String forwardedIp,
                                   @ForAll("ipAddresses") String remoteIp,
                                   @ForAll boolean includeUser) {
        ContactFormRepository contactFormRepository = mock(ContactFormRepository.class);
        ContactInteractionRepository interactionRepository = mock(ContactInteractionRepository.class);
        CarRepository carRepository = mock(CarRepository.class);
        ContactService service = new ContactService(contactFormRepository, interactionRepository, carRepository);

        Dealer dealer = new Dealer();
        dealer.setId(7L);
        dealer.setName("Dealer");
        dealer.setEmail("dealer@example.com");
        dealer.setPhone("+34123456789");

        Car car = new Car();
        car.setId(15L);
        car.setDealer(dealer);

        User user = null;
        if (includeUser) {
            user = new User();
            user.setId(21L);
            user.setName("Buyer");
            user.setEmail("buyer@example.com");
            user.setPassword("secret");
            user.setRole(UserRole.COMPRADOR);
        }

        when(carRepository.findById(15L)).thenReturn(Optional.of(car));
        when(interactionRepository.save(org.mockito.ArgumentMatchers.any(ContactInteraction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIp);
        when(request.getRemoteAddr()).thenReturn(remoteIp);

        ContactInteraction result = service.logInteraction(15L, interactionType, request, user);

        ArgumentCaptor<ContactInteraction> captor = ArgumentCaptor.forClass(ContactInteraction.class);
        verify(interactionRepository).save(captor.capture());
        ContactInteraction saved = captor.getValue();

        assertEquals(interactionType, saved.getInteractionType());
        assertEquals(dealer, saved.getDealer());
        assertEquals(car, saved.getCar());
        assertEquals(user, saved.getUser());
        assertEquals(normalizeIp(forwardedIp, remoteIp), saved.getIpAddress());
        assertNotNull(result);
    }

    @Provide
    net.jqwik.api.Arbitrary<InteractionType> interactionTypes() {
        return Arbitraries.of(InteractionType.values());
    }

    @Provide
    net.jqwik.api.Arbitrary<String> ipAddresses() {
        return Arbitraries.of("203.0.113.8", "198.51.100.10", "", " ");
    }

    private String normalizeIp(String forwardedIp, String remoteIp) {
        if (forwardedIp != null && !forwardedIp.isBlank()) {
            return forwardedIp.split(",")[0].trim();
        }
        return remoteIp;
    }
}

package com.cardealer.service;

import com.cardealer.dto.ContactFormDTO;
import com.cardealer.model.Car;
import com.cardealer.model.ContactInteraction;
import com.cardealer.model.ContactForm;
import com.cardealer.model.User;
import com.cardealer.model.enums.InteractionType;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.ContactInteractionRepository;
import com.cardealer.repository.ContactFormRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

    private final ContactFormRepository contactFormRepository;
    private final ContactInteractionRepository contactInteractionRepository;
    private final CarRepository carRepository;

    /**
     * Get all contact forms
     */
    public List<ContactForm> getAllContactForms() {
        return contactFormRepository.findAllByOrderBySubmittedAtDesc();
    }

    /**
     * Get contact form by ID
     */
    public Optional<ContactForm> getContactFormById(Long id) {
        return contactFormRepository.findById(id);
    }

    /**
     * Get contact forms by email
     */
    public List<ContactForm> getContactFormsByEmail(String email) {
        return contactFormRepository.findByEmail(email);
    }

    /**
     * Save a contact form
     */
    @Transactional
    public ContactForm saveContactForm(ContactFormDTO contactFormDTO) {
        ContactForm contactForm = new ContactForm();
        contactForm.setName(contactFormDTO.getName());
        contactForm.setEmail(contactFormDTO.getEmail());
        contactForm.setSubject(contactFormDTO.getSubject());
        contactForm.setMessage(contactFormDTO.getMessage());

        ContactForm savedForm = contactFormRepository.save(contactForm);
        log.info("Contact form stored for email {}", savedForm.getEmail());
        return savedForm;
    }

    /**
     * Delete a contact form
     */
    @Transactional
    public void deleteContactForm(Long id) {
        contactFormRepository.deleteById(id);
    }

    /**
     * Get total count of contact forms
     */
    public long getTotalContactFormCount() {
        return contactFormRepository.count();
    }

    @Transactional
    public ContactInteraction logInteraction(Long carId, InteractionType interactionType, HttpServletRequest request, User user) {
        Car car = carRepository.findById(carId)
            .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado para registrar interacción"));

        ContactInteraction interaction = new ContactInteraction();
        interaction.setCar(car);
        interaction.setDealer(car.getDealer());
        interaction.setUser(user);
        interaction.setInteractionType(interactionType);
        interaction.setIpAddress(extractIpAddress(request));
        ContactInteraction saved = contactInteractionRepository.save(interaction);
        log.info("Contact interaction logged for car {} via {}", carId, interactionType);
        return saved;
    }

    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

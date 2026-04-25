package com.cardealer.controller;

import com.cardealer.dto.MessageDTO;
import com.cardealer.model.Message;
import com.cardealer.model.User;
import com.cardealer.service.MessageService;
import com.cardealer.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/messages")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public String listMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Authentication authentication) {

        log.info("Loading messages for user: {}", authentication.getName());

        User user = userService.getUserByEmail(authentication.getName());
        PageRequest pageable = PageRequest.of(page, size);
        Page<Message> messagesPage = messageService.getReceivedMessages(user.getId(), pageable);

        model.addAttribute("user", user);
        model.addAttribute("messagesPage", messagesPage);
        model.addAttribute("messages", messagesPage.getContent());
        model.addAttribute("unreadCount", messageService.getUnreadCount(user.getId()));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messagesPage.getTotalPages());

        return "profile-message";
    }

    /**
     * Send a message
     */
    @PostMapping("/send")
    public String sendMessage(
            @Valid @ModelAttribute MessageDTO messageDTO,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        log.info("Processing message send request");
        
        // Validate form
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in message form");
            redirectAttributes.addFlashAttribute("error", "Por favor complete todos los campos requeridos");
            return "redirect:/cars/" + (messageDTO.getCarId() != null ? messageDTO.getCarId() : "");
        }
        
        try {
            // Get authenticated user
            String email = authentication.getName();
            User sender = userService.getUserByEmail(email);
            
            // Send message
            messageService.sendMessage(sender.getId(), messageDTO);
            
            log.info("Message sent successfully from user: {}", email);
            redirectAttributes.addFlashAttribute("success", "Mensaje enviado exitosamente");
            
            // Redirect back to car detail if carId is present
            if (messageDTO.getCarId() != null) {
                return "redirect:/cars/" + messageDTO.getCarId();
            }
            
            return "redirect:/dashboard/messages";
            
        } catch (Exception e) {
            log.error("Error sending message", e);
            redirectAttributes.addFlashAttribute("error", "Error al enviar el mensaje: " + e.getMessage());
            
            if (messageDTO.getCarId() != null) {
                return "redirect:/cars/" + messageDTO.getCarId();
            }
            
            return "redirect:/dashboard/messages";
        }
    }

    /**
     * View message detail
     */
    @GetMapping("/{id}")
    public String viewMessage(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        log.info("Viewing message: {}", id);
        
        try {
            // Get authenticated user
            String email = authentication.getName();
            User user = userService.getUserByEmail(email);
            
            // Get message
            Message message = messageService.getMessageById(id);
            
            // Verify that the user is either sender or receiver
            if (!message.getSender().getId().equals(user.getId()) && 
                !message.getReceiver().getId().equals(user.getId())) {
                log.error("Unauthorized access to message {} by user {}", id, email);
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para ver este mensaje");
                return "redirect:/dashboard/messages";
            }
            
            // Mark as read if user is the receiver and message is unread
            if (message.getReceiver().getId().equals(user.getId()) && !message.getRead()) {
                messageService.markAsRead(id);
            }
            
            model.addAttribute("message", message);
            model.addAttribute("user", user);
            
            return "message-detail";
            
        } catch (Exception e) {
            log.error("Error viewing message", e);
            redirectAttributes.addFlashAttribute("error", "Error al cargar el mensaje: " + e.getMessage());
            return "redirect:/dashboard/messages";
        }
    }

    /**
     * Reply to a message
     */
    @PostMapping("/{id}/reply")
    public String replyMessage(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        log.info("Replying to message: {}", id);
        
        try {
            // Get authenticated user
            String email = authentication.getName();
            User sender = userService.getUserByEmail(email);
            
            // Get original message
            Message originalMessage = messageService.getMessageById(id);
            
            // Create reply
            MessageDTO replyDTO = new MessageDTO();
            replyDTO.setReceiverId(originalMessage.getSender().getId());
            replyDTO.setCarId(originalMessage.getCar() != null ? originalMessage.getCar().getId() : null);
            replyDTO.setSubject("Re: " + originalMessage.getSubject());
            replyDTO.setContent(content);
            
            messageService.sendMessage(sender.getId(), replyDTO);
            
            log.info("Reply sent successfully");
            redirectAttributes.addFlashAttribute("success", "Respuesta enviada exitosamente");
            
            return "redirect:/messages/" + id;
            
        } catch (Exception e) {
            log.error("Error replying to message", e);
            redirectAttributes.addFlashAttribute("error", "Error al enviar la respuesta: " + e.getMessage());
            return "redirect:/messages/" + id;
        }
    }
}

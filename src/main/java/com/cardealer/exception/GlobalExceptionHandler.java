package com.cardealer.exception;

import com.cardealer.service.LocalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizationService localizationService;

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, Locale locale) {
        log.error("Resource not found: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("message", message("error.notFound", locale));
        return "404";
    }

    /**
     * Handle UnauthorizedException
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorized(UnauthorizedException ex, Model model, Locale locale) {
        log.error("Unauthorized access: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 403);
        model.addAttribute("message", message("error.unauthorized", locale));
        return "error";
    }

    /**
     * Handle AccessDeniedException (Spring Security)
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model, Locale locale) {
        log.error("Access denied: {}", ex.getMessage());
        model.addAttribute("error", message("error.accessDenied", locale));
        model.addAttribute("status", 403);
        model.addAttribute("message", message("error.accessDenied", locale));
        return "error";
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateResource(DuplicateResourceException ex, Model model, Locale locale) {
        log.error("Duplicate resource: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 409);
        model.addAttribute("message", message("error.duplicate", locale));
        return "error";
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationErrors(MethodArgumentNotValidException ex, Model model, Locale locale) {
        log.error("Validation error: {}", ex.getMessage());
        populateBindingErrors(model, ex, locale);
        return "error";
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBindException(BindException ex, Model model, Locale locale) {
        log.error("Binding error: {}", ex.getMessage());
        populateBindingErrors(model, ex, locale);
        return "error";
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, Locale locale) {
        log.error("Illegal argument: {}", ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("message", message("error.invalidArgument", locale));
        return "error";
    }

    /**
     * Handle MaxUploadSizeExceededException
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, Model model, Locale locale) {
        log.error("File size exceeded: {}", ex.getMessage());
        model.addAttribute("error", message("error.fileTooLarge", locale));
        model.addAttribute("status", 400);
        model.addAttribute("message", message("error.fileTooLarge", locale));
        return "error";
    }

    @ExceptionHandler(ImageProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleImageProcessing(ImageProcessingException ex, Model model, Locale locale) {
        log.error("Image processing error", ex);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("message", message("error.imageProcessing", locale));
        return "error";
    }

    @ExceptionHandler(LocalizationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleLocalization(LocalizationException ex, Model model, Locale locale) {
        log.error("Localization error", ex);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("status", 500);
        model.addAttribute("message", message("error.localization", locale));
        return "error";
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model, Locale locale) {
        log.error("Unexpected error occurred", ex);
        model.addAttribute("error", message("error.internal", locale));
        model.addAttribute("status", 500);
        model.addAttribute("message", message("error.internal", locale));
        return "error";
    }

    private void populateBindingErrors(Model model, Exception ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            methodArgumentNotValidException.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                errors.put(fieldName, error.getDefaultMessage());
            });
        } else if (ex instanceof BindException bindException) {
            bindException.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                errors.put(fieldName, error.getDefaultMessage());
            });
        }
        model.addAttribute("errors", errors);
        model.addAttribute("status", 400);
        model.addAttribute("message", message("error.validation", locale));
        model.addAttribute("error", message("error.validation", locale));
    }

    private String message(String key, Locale locale) {
        try {
            return localizationService.getMessage(key, locale);
        } catch (Exception ignored) {
            return key;
        }
    }
}

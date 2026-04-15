package com.cardealer.validation;

import com.cardealer.dto.CarDTO;
import com.cardealer.util.FileUploadUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ValidImageUploadValidator implements ConstraintValidator<ValidImageUpload, CarDTO> {

    @Override
    public boolean isValid(CarDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        long newImages = countPresentFiles(value.getImageFiles());
        long existingImages = value.getExistingImages() == null ? 0 : value.getExistingImages().stream().filter(img -> img != null && !img.isBlank()).count();
        long totalImages = newImages + existingImages;

        boolean validCount = totalImages >= FileUploadUtil.MIN_IMAGE_COUNT && totalImages <= FileUploadUtil.MAX_IMAGE_COUNT;
        if (!validCount) {
            addViolation(context, "Debe mantener o subir entre 20 y 25 imágenes", "imageFiles");
            return false;
        }

        if (value.getImageFiles() != null) {
            for (MultipartFile file : value.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    String contentType = file.getContentType();
                    boolean validType = contentType != null && List.of("image/jpeg", "image/png", "image/webp").contains(contentType.toLowerCase());
                    boolean validSize = file.getSize() <= 10 * 1024 * 1024L;
                    if (!validType || !validSize) {
                        addViolation(context, "Cada imagen debe ser JPEG, PNG o WebP y no superar 10MB", "imageFiles");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private long countPresentFiles(List<MultipartFile> files) {
        return files == null ? 0 : files.stream().filter(file -> file != null && !file.isEmpty()).count();
    }

    private void addViolation(ConstraintValidatorContext context, String message, String property) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addPropertyNode(property)
            .addConstraintViolation();
    }
}

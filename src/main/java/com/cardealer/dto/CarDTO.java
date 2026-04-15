package com.cardealer.dto;

import com.cardealer.validation.ValidImageUpload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidImageUpload
public class CarDTO {
    
    @NotBlank(message = "La marca es obligatoria")
    private String brand;
    
    @NotBlank(message = "El modelo es obligatorio")
    private String model;
    
    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "El año debe ser mayor a 1900")
    private Integer year;
    
    @NotNull(message = "El precio es obligatorio")
    private BigDecimal price;
    
    @Min(value = 0, message = "El kilometraje debe ser positivo")
    private Integer mileage;
    
    @NotBlank(message = "El tipo de combustible es obligatorio")
    private String fuelType;
    
    @NotBlank(message = "El tipo de transmisión es obligatorio")
    private String transmission;
    
    private String bodyType;
    
    @NotBlank(message = "La condición es obligatoria")
    private String condition;

    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    private String color;
    
    @Min(value = 2, message = "El número de puertas debe ser al menos 2")
    private Integer doors;
    
    private String engine;
    
    private String description;
    
    private List<String> features;

    private List<MultipartFile> imageFiles;

    // For editing - existing image URLs
    private List<String> existingImages;

    private String locale;
}

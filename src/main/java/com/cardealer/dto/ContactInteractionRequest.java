package com.cardealer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContactInteractionRequest {

    @NotNull
    private Long carId;

    @NotNull
    private String interactionType;
}

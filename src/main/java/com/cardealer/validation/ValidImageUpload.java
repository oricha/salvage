package com.cardealer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidImageUploadValidator.class)
public @interface ValidImageUpload {
    String message() default "Debe mantener o subir entre 20 y 25 imágenes válidas";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

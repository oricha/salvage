package com.cardealer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CarSalvage {

    public static void main(String[] args) {
        SpringApplication.run(CarSalvage.class, args);
    }
}

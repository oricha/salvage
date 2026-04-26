package com.cardealer.model;

import com.cardealer.model.enums.BodyType;
import com.cardealer.model.enums.CarCondition;
import com.cardealer.model.enums.FuelType;
import com.cardealer.model.enums.TransmissionType;
import com.cardealer.model.enums.VehicleCategory;
import com.cardealer.model.enums.VehicleOrigin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    private String variant;

    @Column(name = "car_year", nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal exportPrice;

    @Column(nullable = false)
    private Integer mileage;

    @Column(nullable = false)
    private String color;

    @Column(length = 32)
    private String colorCode;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    private Boolean refinedFuelType;

    @Enumerated(EnumType.STRING)
    private TransmissionType transmission;

    @Enumerated(EnumType.STRING)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    private CarCondition condition;

    @Enumerated(EnumType.STRING)
    private VehicleOrigin origin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleCategory category = VehicleCategory.PASSENGER_CAR;

    private Integer doors;

    private String engine;
    private Integer powerHp;
    private Boolean registrationAvailable;
    private Boolean awaitingVerification;
    private Boolean fullInstructionBooklet;
    private Boolean allKeysAvailable;
    private Boolean engineDamage;
    private Boolean lowerDamage;
    private Boolean drivable;
    private Boolean movable;
    private Boolean engineRuns;
    private Boolean airbagsIntact;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 5)
    private String locale = "es";

    @ElementCollection
    @CollectionTable(name = "car_features", joinColumns = @JoinColumn(name = "car_id"))
    @Column(name = "feature")
    private List<String> features;

    @ElementCollection
    @CollectionTable(name = "car_images", joinColumns = @JoinColumn(name = "car_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url")
    private List<String> images;

    @Column(nullable = false)
    private Integer views = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateImageCount() {
        if (category == null) {
            category = VehicleCategory.PASSENGER_CAR;
        }

        // Preserve legacy rows without migrated galleries while enforcing the new constraint
        // for listings that already carry uploaded images.
        if (images != null && !images.isEmpty() && (images.size() < 20 || images.size() > 25)) {
            throw new IllegalStateException("Cada vehículo debe tener entre 20 y 25 imágenes");
        }
    }
}

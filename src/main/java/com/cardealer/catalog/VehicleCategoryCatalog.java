package com.cardealer.catalog;

import com.cardealer.model.enums.VehicleCategory;

import java.util.List;
import java.util.Map;

public final class VehicleCategoryCatalog {

    private VehicleCategoryCatalog() {
    }

    public static List<Map<String, Object>> primaryCategories() {
        return List.of(
            category(VehicleCategory.PASSENGER_CAR, "far fa-car-side"),
            category(VehicleCategory.COMMERCIAL_VEHICLE, "far fa-van-shuttle"),
            category(VehicleCategory.MOTORCYCLE, "far fa-motorcycle"),
            category(VehicleCategory.CAMPER, "far fa-caravan"),
            category(VehicleCategory.TRUCK, "far fa-truck")
        );
    }

    public static List<Map<String, Object>> secondaryCategories() {
        return List.of(
            category(VehicleCategory.CARAVAN, null),
            category(VehicleCategory.TRAILER, null),
            category(VehicleCategory.LORRY, null),
            category(VehicleCategory.MOPED, null),
            category(VehicleCategory.MICROCAR, null),
            category(VehicleCategory.TAXI, null),
            category(VehicleCategory.BUS, null),
            category(VehicleCategory.MACHINE, null),
            category(VehicleCategory.BICYCLE, null),
            category(VehicleCategory.OTHER, null),
            category(VehicleCategory.DAMAGED, null),
            category(VehicleCategory.SALVAGE, null)
        );
    }

    private static Map<String, Object> category(VehicleCategory category, String icon) {
        return Map.of(
            "key", category,
            "labelKey", category.getMessageKey(),
            "icon", icon == null ? "" : icon,
            "url", "/cars?categories=" + category.name()
        );
    }
}

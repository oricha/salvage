package com.cardealer.model.enums;

public enum VehicleCategory {
    PASSENGER_CAR,
    COMMERCIAL_VEHICLE,
    MOTORCYCLE,
    CAMPER,
    CARAVAN,
    TRUCK,
    LORRY,
    TRAILER,
    MOPED,
    MICROCAR,
    TAXI,
    BUS,
    MACHINE,
    BICYCLE,
    OTHER,
    DAMAGED,
    SALVAGE;

    public String getMessageKey() {
        return "vehicleCategory." + name().toLowerCase();
    }
}

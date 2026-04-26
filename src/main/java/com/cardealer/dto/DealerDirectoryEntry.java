package com.cardealer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DealerDirectoryEntry {

    private final Long dealerId;
    private final String companyName;
    private final String specialization;
    private final String region;
    private final String city;
    private final String email;
    private final String phone;
    private final String logoUrl;
    private final Integer listingCount;
    private final boolean realDealer;

    public String getDisplayLocation() {
        if (city != null && !city.isBlank() && region != null && !region.isBlank()) {
            return city + ", " + region;
        }
        if (city != null && !city.isBlank()) {
            return city;
        }
        return region;
    }

    public String getAlphabetLetter() {
        if (companyName == null || companyName.isBlank()) {
            return "#";
        }
        return companyName.substring(0, 1).toUpperCase();
    }
}

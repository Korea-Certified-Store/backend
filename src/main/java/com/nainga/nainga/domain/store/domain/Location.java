package com.nainga.nainga.domain.store.domain;

import lombok.Getter;

@Getter
public class Location {
    private final double longitude;
    private final double latitude;

    public Location(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}

package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class Location {

    private final long locationId;
    private final double latitude;
    private final double longitude;
    private final String address;
    private final String city;
    private final String country;

    public Location(long locationId, double latitude, double longitude, String address, String city, String country) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.country = country;
    }

    public long getLocationId() {
        return locationId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}

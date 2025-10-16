package com.example.food.model;

public class Restaurant {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String category;
    private String region;

    // No-args constructor (required by Firebase)
    public Restaurant() {
    }

    // Full constructor
    public Restaurant(String id, String name, String address, double latitude, double longitude, String category, String region) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.region = region;
    }

    // Simplified constructor (for backward compatibility)
    public Restaurant(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = "Restaurant";
        this.region = "Melbourne";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", category='" + category + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}

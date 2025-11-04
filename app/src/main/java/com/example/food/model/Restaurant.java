package com.example.food.model;

/**
 * Restaurant - Data model representing a restaurant
 * 
 * This class stores restaurant information:
 * - Basic info (ID, name, address)
 * - Geographic location (latitude, longitude)
 * - Classification (category, region)
 * 
 * Used for displaying restaurants on maps and in reviews
 */

public class Restaurant {
    // Restaurant data fields
    private String id; // Unique restaurant ID
    private String name; // Restaurant name
    private String address; // Street address
    private double latitude; // GPS latitude coordinate
    private double longitude; // GPS longitude coordinate
    private String category; // Cuisine type (e.g., "Italian", "Chinese")
    private String region; // Location region (e.g., "Melbourne CBD")

    /**
     * No-args constructor required by Firebase Firestore
     */
    public Restaurant() {
    }

    /**
     * Full constructor with all fields
     */
    public Restaurant(String id, String name, String address, double latitude, double longitude, String category, String region) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.region = region;
    }

    /**
     * Simplified constructor for backward compatibility
     * Uses default values for category and region
     */
    public Restaurant(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = "Restaurant"; // Default category
        this.region = "Melbourne"; // Default region
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

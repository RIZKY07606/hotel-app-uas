package com.example.hotelapp;

public class Hotel {
    private String name;
    private String address;
    private String imageUrl;
    private double price;
    private float rating;

    // Constructor
    public Hotel() {}

    public Hotel(String name, String address, String imageUrl, double price, float rating) {
        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
    }

    // Getters and Setters
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}

package com.dsl.controller;

public class Vehicle {
    private String make;
    private String model;
    private Integer year;
    private String color;
    private String vin;
    private Integer mileage;
    
    public Vehicle() {}
    
    public Vehicle(String make, String model, Integer year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }
    
    // Getters and setters
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
}
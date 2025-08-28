package com.dsl.controller;

public class UserContext {
    private Long customerId;
    private String customerName;
    private Vehicle vehicle;
    private String currentService;
    private String conversationState;
    
    public UserContext() {}
    
    // Getters and setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    
    public String getCurrentService() { return currentService; }
    public void setCurrentService(String currentService) { this.currentService = currentService; }
    
    public String getConversationState() { return conversationState; }
    public void setConversationState(String conversationState) { this.conversationState = conversationState; }
}

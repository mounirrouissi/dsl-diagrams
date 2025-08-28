package com.dsl.controller;

import java.util.Map;

public class NLPResult {
    private String intent;
    private Map<String, String> entities;
    private double confidence;
    
    public NLPResult() {}
    
    public NLPResult(String intent, Map<String, String> entities, double confidence) {
        this.intent = intent;
        this.entities = entities;
        this.confidence = confidence;
    }
    
    // Getters and setters
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public Map<String, String> getEntities() { return entities; }
    public void setEntities(Map<String, String> entities) { this.entities = entities; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
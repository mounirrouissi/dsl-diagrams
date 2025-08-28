package com.dsl.controller;

public class QuickReply {
    private String text;
    private String label;
    
    public QuickReply() {}
    
    public QuickReply(String text, String label) {
        this.text = text;
        this.label = label;
    }
    
    public QuickReply(String text) {
        this.text = text;
        this.label = text;
    }
    
    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
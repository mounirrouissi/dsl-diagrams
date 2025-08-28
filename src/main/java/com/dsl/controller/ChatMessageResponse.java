package com.dsl.controller;

import java.util.List;

public class ChatMessageResponse {
    private String message;
    private String intent;
    private UserContext context;
    private List<QuickReply> quickReplies;
    private String timestamp;
    
    public ChatMessageResponse() {}
    
    public ChatMessageResponse(String message, String intent, UserContext context, List<QuickReply> quickReplies) {
        this.message = message;
        this.intent = intent;
        this.context = context;
        this.quickReplies = quickReplies;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public UserContext getContext() { return context; }
    public void setContext(UserContext context) { this.context = context; }
    
    public List<QuickReply> getQuickReplies() { return quickReplies; }
    public void setQuickReplies(List<QuickReply> quickReplies) { this.quickReplies = quickReplies; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
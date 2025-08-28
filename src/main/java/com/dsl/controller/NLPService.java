package com.dsl.controller;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NLPService {
    
    // Intent patterns (simple keyword-based for now)
    private final Map<String, List<String>> intentKeywords = new HashMap<>();
    
    // Entity extraction patterns
    private final Map<String, Pattern> entityPatterns = new HashMap<>();
    
    public NLPService() {
        initializeIntentKeywords();
        initializeEntityPatterns();
    }
    
    private void initializeIntentKeywords() {
        // Greeting intents
        intentKeywords.put("greeting", Arrays.asList(
            "hello", "hi", "hey", "good morning", "good afternoon", "good evening"
        ));
        
        // Name provision
        intentKeywords.put("provide_name", Arrays.asList(
            "my name is", "i'm", "i am", "call me", "name's"
        ));
        
        // Vehicle information
        intentKeywords.put("vehicle_info", Arrays.asList(
            "my car", "i drive", "i have a", "my vehicle", "car is", "drive a"
        ));
        
        // Service inquiries
        intentKeywords.put("service_inquiry", Arrays.asList(
            "oil change", "tire rotation", "brake", "maintenance", "service", 
            "check", "inspection", "repair", "fix"
        ));
        
        // Booking requests
        intentKeywords.put("booking_request", Arrays.asList(
            "schedule", "book", "appointment", "reserve", "when can", "available"
        ));
        
        // Confirmations
        intentKeywords.put("confirmation", Arrays.asList(
            "yes", "yeah", "yep", "correct", "right", "that's right", "confirm"
        ));
        
        // Negations
        intentKeywords.put("negation", Arrays.asList(
            "no", "nope", "not", "incorrect", "wrong"
        ));
    }
    
    private void initializeEntityPatterns() {
        // Person names (simple pattern)
        entityPatterns.put("person_name", Pattern.compile(
            "(?:my name is|i'm|i am|call me)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?)", 
            Pattern.CASE_INSENSITIVE
        ));
        
        // Vehicle make/model/year patterns
        entityPatterns.put("vehicle_year", Pattern.compile("\\b(19|20)\\d{2}\\b"));
        
        // Common car makes
        entityPatterns.put("vehicle_make", Pattern.compile(
            "\\b(toyota|honda|ford|chevrolet|chevy|nissan|hyundai|kia|mazda|subaru|bmw|mercedes|audi|volkswagen|vw)\\b",
            Pattern.CASE_INSENSITIVE
        ));
        
        // Service types
        entityPatterns.put("service_type", Pattern.compile(
            "\\b(oil change|tire rotation|brake inspection|brake check|maintenance|tune up|alignment)\\b",
            Pattern.CASE_INSENSITIVE
        ));
        
        // Time expressions
        entityPatterns.put("time_expression", Pattern.compile(
            "\\b(tomorrow|today|next week|this week|morning|afternoon|evening|\\d{1,2}:\\d{2}|\\d{1,2}\\s*(?:am|pm))\\b",
            Pattern.CASE_INSENSITIVE
        ));
    }
    
    public NLPResult processMessage(String message, UserContext context) {
        String cleanMessage = message.toLowerCase().trim();
        
        // Determine intent
        String intent = classifyIntent(cleanMessage, context);
        
        // Extract entities
        Map<String, String> entities = extractEntities(message);
        
        // Calculate confidence (simple heuristic)
        double confidence = calculateConfidence(intent, entities, cleanMessage);
        
        return new NLPResult(intent, entities, confidence);
    }
    
    private String classifyIntent(String message, UserContext context) {
        // Context-aware intent classification
        String conversationState = context != null ? context.getConversationState() : null;
        
        // If we're waiting for specific information, prioritize those intents
        if ("awaiting_name".equals(conversationState)) {
            if (containsPersonName(message)) {
                return "provide_name";
            }
        }
        
        if ("name_provided".equals(conversationState) || "awaiting_vehicle".equals(conversationState)) {
            if (containsVehicleInfo(message)) {
                return "vehicle_info";
            }
        }
        
        // General intent classification based on keywords
        Map<String, Integer> intentScores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : intentKeywords.entrySet()) {
            String intent = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int score = 0;
            for (String keyword : keywords) {
                if (message.contains(keyword)) {
                    score += keyword.split("\\s+").length; // Multi-word phrases get higher scores
                }
            }
            
            if (score > 0) {
                intentScores.put(intent, score);
            }
        }
        
        // Return the intent with the highest score
        return intentScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    private Map<String, String> extractEntities(String message) {
        Map<String, String> entities = new HashMap<>();
        
        for (Map.Entry<String, Pattern> entry : entityPatterns.entrySet()) {
            String entityType = entry.getKey();
            Pattern pattern = entry.getValue();
            
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String value = matcher.group(1);
                if (value != null) {
                    entities.put(entityType, value.trim());
                } else {
                    entities.put(entityType, matcher.group().trim());
                }
            }
        }
        
        // Special handling for vehicle model (more complex extraction)
        extractVehicleModel(message, entities);
        
        return entities;
    }
    
    private void extractVehicleModel(String message, Map<String, String> entities) {
        // Common vehicle models by make
        Map<String, List<String>> modelsByMake = new HashMap<>();
        modelsByMake.put("honda", Arrays.asList("civic", "accord", "crv", "pilot", "fit"));
        modelsByMake.put("toyota", Arrays.asList("camry", "corolla", "prius", "rav4", "highlander"));
        modelsByMake.put("ford", Arrays.asList("f150", "f-150", "mustang", "focus", "escape", "explorer"));
        modelsByMake.put("chevrolet", Arrays.asList("silverado", "malibu", "equinox", "tahoe"));
        modelsByMake.put("chevy", Arrays.asList("silverado", "malibu", "equinox", "tahoe"));
        
        String make = entities.get("vehicle_make");
        if (make != null) {
            List<String> models = modelsByMake.get(make.toLowerCase());
            if (models != null) {
                for (String model : models) {
                    if (message.toLowerCase().contains(model)) {
                        entities.put("vehicle_model", model);
                        break;
                    }
                }
            }
        }
    }
    
    private boolean containsPersonName(String message) {
        // Simple heuristic: check for "my name is" patterns or standalone capitalized words
        Pattern namePattern = Pattern.compile("(?:my name is|i'm|i am|call me)\\s+([A-Z][a-z]+)", Pattern.CASE_INSENSITIVE);
        return namePattern.matcher(message).find() || 
               Pattern.compile("^[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?$").matcher(message.trim()).find();
    }
    
    private boolean containsVehicleInfo(String message) {
        // Check if message contains year + make or make + model patterns
        return entityPatterns.get("vehicle_year").matcher(message).find() ||
               entityPatterns.get("vehicle_make").matcher(message).find() ||
               message.toLowerCase().matches(".*\\b(car|vehicle|drive|have)\\b.*");
    }
    
    private double calculateConfidence(String intent, Map<String, String> entities, String message) {
        double confidence = 0.5; // Base confidence
        
        // Increase confidence based on entities found
        confidence += entities.size() * 0.1;
        
        // Increase confidence for known intents
        if (!"unknown".equals(intent)) {
            confidence += 0.3;
        }
        
        // Increase confidence for longer, more specific messages
        if (message.length() > 20) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 1.0);
    }
}
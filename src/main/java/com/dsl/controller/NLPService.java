package com.dsl.controller;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NLPService {
    
    // Intent patterns with weighted keywords
    private final Map<String, Map<String, Double>> intentKeywords = new HashMap<>();
    
    // Entity extraction patterns
    private final Map<String, Pattern> entityPatterns = new HashMap<>();
    
    // Sentiment analysis patterns
    private final Map<String, List<String>> sentimentPatterns = new HashMap<>();
    
    // Context-aware response templates
    private final Map<String, List<String>> responseTemplates = new HashMap<>();
    
    // Conversation flow states
    private final Map<String, List<String>> conversationFlows = new HashMap<>();
    
    // Spell correction dictionary
    private final Map<String, String> commonMisspellings = new HashMap<>();
    
    public NLPService() {
        initializeIntentKeywords();
        initializeEntityPatterns();
        initializeSentimentPatterns();
        initializeResponseTemplates();
        initializeConversationFlows();
        initializeSpellCorrection();
    }
    
    private void initializeIntentKeywords() {
        // Greeting intents with weights
        Map<String, Double> greetingWeights = new HashMap<>();
        greetingWeights.put("hello", 1.0);
        greetingWeights.put("hi", 1.0);
        greetingWeights.put("hey", 0.8);
        greetingWeights.put("good morning", 1.2);
        greetingWeights.put("good afternoon", 1.2);
        greetingWeights.put("good evening", 1.2);
        greetingWeights.put("howdy", 0.9);
        greetingWeights.put("greetings", 1.1);
        intentKeywords.put("greeting", greetingWeights);
        
        // Name provision
        Map<String, Double> nameWeights = new HashMap<>();
        nameWeights.put("my name is", 1.5);
        nameWeights.put("i'm", 1.2);
        nameWeights.put("i am", 1.2);
        nameWeights.put("call me", 1.3);
        nameWeights.put("name's", 1.1);
        nameWeights.put("this is", 0.8);
        intentKeywords.put("provide_name", nameWeights);
        
        // Vehicle information
        Map<String, Double> vehicleWeights = new HashMap<>();
        vehicleWeights.put("my car", 1.3);
        vehicleWeights.put("i drive", 1.2);
        vehicleWeights.put("i have a", 1.0);
        vehicleWeights.put("my vehicle", 1.3);
        vehicleWeights.put("car is", 1.1);
        vehicleWeights.put("drive a", 1.1);
        vehicleWeights.put("own a", 1.0);
        vehicleWeights.put("got a", 0.9);
        intentKeywords.put("vehicle_info", vehicleWeights);
        
        // Service inquiries
        Map<String, Double> serviceWeights = new HashMap<>();
        serviceWeights.put("oil change", 1.5);
        serviceWeights.put("tire rotation", 1.4);
        serviceWeights.put("brake", 1.3);
        serviceWeights.put("maintenance", 1.2);
        serviceWeights.put("service", 1.0);
        serviceWeights.put("check", 0.8);
        serviceWeights.put("inspection", 1.3);
        serviceWeights.put("repair", 1.2);
        serviceWeights.put("fix", 1.1);
        serviceWeights.put("tune up", 1.3);
        serviceWeights.put("alignment", 1.4);
        intentKeywords.put("service_inquiry", serviceWeights);
        
        // Booking requests
        Map<String, Double> bookingWeights = new HashMap<>();
        bookingWeights.put("schedule", 1.4);
        bookingWeights.put("book", 1.3);
        bookingWeights.put("appointment", 1.5);
        bookingWeights.put("reserve", 1.2);
        bookingWeights.put("when can", 1.1);
        bookingWeights.put("available", 1.0);
        bookingWeights.put("set up", 1.1);
        bookingWeights.put("arrange", 1.0);
        intentKeywords.put("booking_request", bookingWeights);
        
        // Confirmations
        Map<String, Double> confirmWeights = new HashMap<>();
        confirmWeights.put("yes", 1.5);
        confirmWeights.put("yeah", 1.2);
        confirmWeights.put("yep", 1.1);
        confirmWeights.put("correct", 1.3);
        confirmWeights.put("right", 1.2);
        confirmWeights.put("that's right", 1.4);
        confirmWeights.put("confirm", 1.5);
        confirmWeights.put("absolutely", 1.3);
        confirmWeights.put("definitely", 1.3);
        intentKeywords.put("confirmation", confirmWeights);
        
        // Negations
        Map<String, Double> negationWeights = new HashMap<>();
        negationWeights.put("no", 1.5);
        negationWeights.put("nope", 1.2);
        negationWeights.put("not", 1.0);
        negationWeights.put("incorrect", 1.3);
        negationWeights.put("wrong", 1.2);
        negationWeights.put("never mind", 1.1);
        negationWeights.put("cancel", 1.4);
        intentKeywords.put("negation", negationWeights);
        
        // Emergency/Urgent
        Map<String, Double> urgentWeights = new HashMap<>();
        urgentWeights.put("emergency", 2.0);
        urgentWeights.put("urgent", 1.8);
        urgentWeights.put("asap", 1.6);
        urgentWeights.put("immediately", 1.7);
        urgentWeights.put("right now", 1.5);
        urgentWeights.put("broke down", 1.9);
        urgentWeights.put("won't start", 1.8);
        intentKeywords.put("emergency", urgentWeights);
        
        // Complaint/Issue
        Map<String, Double> complaintWeights = new HashMap<>();
        complaintWeights.put("problem", 1.4);
        complaintWeights.put("issue", 1.3);
        complaintWeights.put("complaint", 1.6);
        complaintWeights.put("unhappy", 1.5);
        complaintWeights.put("dissatisfied", 1.6);
        complaintWeights.put("terrible", 1.7);
        complaintWeights.put("awful", 1.6);
        intentKeywords.put("complaint", complaintWeights);
    }
    
    private void initializeEntityPatterns() {
        // Enhanced person names pattern
        entityPatterns.put("person_name", Pattern.compile(
            "(?:my name is|i'm|i am|call me|this is|name's)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?)", 
            Pattern.CASE_INSENSITIVE
        ));
        
        // Vehicle year with broader range
        entityPatterns.put("vehicle_year", Pattern.compile("\\b(19[5-9]\\d|20[0-4]\\d)\\b"));
        
        // Expanded car makes
        entityPatterns.put("vehicle_make", Pattern.compile(
            "\\b(toyota|honda|ford|chevrolet|chevy|nissan|hyundai|kia|mazda|subaru|bmw|mercedes|audi|volkswagen|vw|lexus|acura|infiniti|cadillac|buick|gmc|jeep|dodge|chrysler|ram|lincoln|volvo|jaguar|land rover|porsche|tesla|mitsubishi)\\b",
            Pattern.CASE_INSENSITIVE
        ));
        
        // Enhanced service types
        entityPatterns.put("service_type", Pattern.compile(
            "\\b(oil change|tire rotation|brake inspection|brake check|brake repair|maintenance|tune up|alignment|transmission|engine|battery|alternator|starter|radiator|coolant|air filter|cabin filter|spark plugs|timing belt|serpentine belt|exhaust|muffler|catalytic converter|suspension|shocks|struts|cv joint|differential|power steering|air conditioning|ac repair|heating|diagnostic|inspection|smog check|emissions test)\\b",
            Pattern.CASE_INSENSITIVE
        ));
        
        // Enhanced time expressions
        entityPatterns.put("time_expression", Pattern.compile(
            "\\b(tomorrow|today|yesterday|next week|this week|next month|morning|afternoon|evening|noon|midnight|\\d{1,2}:\\d{2}\\s*(?:am|pm)?|\\d{1,2}\\s*(?:am|pm)|monday|tuesday|wednesday|thursday|friday|saturday|sunday|weekday|weekend)\\b",
            Pattern.CASE_INSENSITIVE
        ));
        
        // Phone numbers
        entityPatterns.put("phone_number", Pattern.compile(
            "\\b(?:\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b"
        ));
        
        // Email addresses
        entityPatterns.put("email", Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
        ));
        
        // Mileage
        entityPatterns.put("mileage", Pattern.compile(
            "\\b(\\d{1,3}(?:,\\d{3})*|\\d+)\\s*(?:miles?|mi|k|km)\\b",
            Pattern.CASE_INSENSITIVE
        ));
        
        // Price/Cost
        entityPatterns.put("price", Pattern.compile(
            "\\$\\s*(\\d+(?:\\.\\d{2})?)|\\b(\\d+)\\s*(?:dollars?|bucks?)\\b",
            Pattern.CASE_INSENSITIVE
        ));
    }
    
    private void initializeSentimentPatterns() {
        sentimentPatterns.put("positive", Arrays.asList(
            "great", "excellent", "awesome", "fantastic", "wonderful", "amazing", 
            "perfect", "love", "happy", "satisfied", "pleased", "good", "nice",
            "thank you", "thanks", "appreciate", "helpful"
        ));
        
        sentimentPatterns.put("negative", Arrays.asList(
            "terrible", "awful", "horrible", "bad", "worst", "hate", "angry",
            "frustrated", "disappointed", "unhappy", "dissatisfied", "problem",
            "issue", "complaint", "wrong", "broken", "failed"
        ));
        
        sentimentPatterns.put("neutral", Arrays.asList(
            "okay", "fine", "alright", "maybe", "perhaps", "possibly", "unsure"
        ));
    }
    
    private void initializeResponseTemplates() {
        responseTemplates.put("greeting_new", Arrays.asList(
            "Hello! I'm your auto service assistant. I'm here to help with vehicle maintenance, service bookings, and answer questions about your car. What's your name?",
            "Hi there! Welcome to our auto service center. I can help you schedule appointments, answer service questions, and more. May I have your name?",
            "Good day! I'm here to assist with all your automotive needs. Let's start with your name so I can provide personalized service."
        ));
        
        responseTemplates.put("greeting_returning", Arrays.asList(
            "Welcome back, {name}! Great to see you again. How can I help with your {vehicle} today?",
            "Hello {name}! I see you have a {vehicle}. What service do you need today?",
            "Hi {name}! Ready to take care of your {vehicle}? What can I help you with?"
        ));
        
        responseTemplates.put("emergency_response", Arrays.asList(
            "I understand this is urgent. Let me connect you with our emergency service team right away.",
            "This sounds like an emergency situation. I'm prioritizing your request and will get you immediate assistance.",
            "I can see this needs immediate attention. Let me get our emergency team to help you right now."
        ));
    }
    
    private void initializeConversationFlows() {
        conversationFlows.put("new_customer", Arrays.asList(
            "greeting", "provide_name", "vehicle_info", "service_inquiry", "booking_request"
        ));
        
        conversationFlows.put("returning_customer", Arrays.asList(
            "greeting", "service_inquiry", "booking_request"
        ));
        
        conversationFlows.put("emergency_flow", Arrays.asList(
            "emergency", "vehicle_info", "location", "immediate_assistance"
        ));
    }
    
    private void initializeSpellCorrection() {
        commonMisspellings.put("oilchange", "oil change");
        commonMisspellings.put("tirerotation", "tire rotation");
        commonMisspellings.put("brakecheck", "brake check");
        commonMisspellings.put("maintenence", "maintenance");
        commonMisspellings.put("appointement", "appointment");
        commonMisspellings.put("scedule", "schedule");
        commonMisspellings.put("vehical", "vehicle");
        commonMisspellings.put("repare", "repair");
        commonMisspellings.put("servise", "service");
        commonMisspellings.put("inspectoin", "inspection");
    }
    
    public NLPResult processMessage(String message, UserContext context) {
        // Preprocess message
        String preprocessedMessage = preprocessMessage(message);
        String cleanMessage = preprocessedMessage.toLowerCase().trim();
        
        // Determine intent with confidence scores
        Map<String, Double> intentConfidences = classifyIntentWithConfidence(cleanMessage, context);
        String primaryIntent = getBestIntent(intentConfidences);
        
        // Extract entities
        Map<String, String> entities = extractEntities(preprocessedMessage);
        
        // Analyze sentiment
        String sentiment = analyzeSentiment(cleanMessage);
        
        // Calculate overall confidence
        double confidence = calculateAdvancedConfidence(primaryIntent, entities, cleanMessage, intentConfidences);
        
        // Determine if human handoff is needed
        boolean requiresHumanHandoff = shouldHandoffToHuman(primaryIntent, sentiment, confidence, context);
        
        // Generate suggested response
        String suggestedResponse = generateSuggestedResponse(primaryIntent, entities, context, sentiment);
        
        NLPResult result = new NLPResult();
        result.setIntent(primaryIntent);
        result.setEntities(entities);
        result.setConfidence(confidence);
        result.setSentiment(sentiment);
        result.setIntentConfidences(intentConfidences);
        result.setRequiresHumanHandoff(requiresHumanHandoff);
        result.setSuggestedResponse(suggestedResponse);
        
        return result;
    }
    
    private String preprocessMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }
        
        String processed = message.trim();
        
        // Apply spell correction
        for (Map.Entry<String, String> correction : commonMisspellings.entrySet()) {
            processed = processed.replaceAll("\\b" + correction.getKey() + "\\b", correction.getValue());
        }
        
        // Normalize whitespace
        processed = processed.replaceAll("\\s+", " ");
        
        // Handle common abbreviations
        processed = processed.replaceAll("\\bu\\b", "you");
        processed = processed.replaceAll("\\bur\\b", "your");
        processed = processed.replaceAll("\\br\\b", "are");
        processed = processed.replaceAll("\\btmrw\\b", "tomorrow");
        
        return processed;
    }
    
    private Map<String, Double> classifyIntentWithConfidence(String message, UserContext context) {
        Map<String, Double> intentScores = new HashMap<>();
        
        // Context-aware intent classification
        String conversationState = context != null ? context.getConversationState() : null;
        
        // Apply context boosting
        if ("awaiting_name".equals(conversationState) && containsPersonName(message)) {
            intentScores.put("provide_name", 0.9);
        }
        
        if (("name_provided".equals(conversationState) || "awaiting_vehicle".equals(conversationState)) 
            && containsVehicleInfo(message)) {
            intentScores.put("vehicle_info", 0.9);
        }
        
        // Enhanced intent classification with weighted keywords
        for (Map.Entry<String, Map<String, Double>> entry : intentKeywords.entrySet()) {
            String intent = entry.getKey();
            Map<String, Double> keywords = entry.getValue();
            
            double score = 0.0;
            int matchCount = 0;
            
            for (Map.Entry<String, Double> keywordEntry : keywords.entrySet()) {
                String keyword = keywordEntry.getKey();
                Double weight = keywordEntry.getValue();
                
                if (message.contains(keyword)) {
                    score += weight;
                    matchCount++;
                }
            }
            
            // Normalize score based on message length and match density
            if (score > 0) {
                double normalizedScore = score / Math.max(1, message.split("\\s+").length / 5.0);
                normalizedScore *= Math.min(1.0, matchCount / 3.0 + 0.5); // Boost for multiple matches
                intentScores.put(intent, Math.min(1.0, normalizedScore));
            }
        }
        
        // If no strong matches, check for partial matches
        if (intentScores.isEmpty() || intentScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0) < 0.3) {
            intentScores.putAll(findPartialMatches(message));
        }
        
        return intentScores;
    }
    
    private String getBestIntent(Map<String, Double> intentConfidences) {
        return intentConfidences.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("unknown");
    }
    
    private Map<String, Double> findPartialMatches(String message) {
        Map<String, Double> partialMatches = new HashMap<>();
        
        // Look for partial keyword matches
        for (Map.Entry<String, Map<String, Double>> entry : intentKeywords.entrySet()) {
            String intent = entry.getKey();
            Map<String, Double> keywords = entry.getValue();
            
            double partialScore = 0.0;
            for (String keyword : keywords.keySet()) {
                String[] keywordParts = keyword.split("\\s+");
                for (String part : keywordParts) {
                    if (part.length() > 3 && message.contains(part)) {
                        partialScore += 0.3; // Lower score for partial matches
                    }
                }
            }
            
            if (partialScore > 0) {
                partialMatches.put(intent, Math.min(0.6, partialScore)); // Cap partial matches
            }
        }
        
        return partialMatches;
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
    
    private String analyzeSentiment(String message) {
        Map<String, Integer> sentimentScores = new HashMap<>();
        sentimentScores.put("positive", 0);
        sentimentScores.put("negative", 0);
        sentimentScores.put("neutral", 0);
        
        for (Map.Entry<String, List<String>> entry : sentimentPatterns.entrySet()) {
            String sentiment = entry.getKey();
            List<String> patterns = entry.getValue();
            
            for (String pattern : patterns) {
                if (message.contains(pattern)) {
                    sentimentScores.put(sentiment, sentimentScores.get(sentiment) + 1);
                }
            }
        }
        
        // Return sentiment with highest score
        return sentimentScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("neutral");
    }
    
    private double calculateAdvancedConfidence(String intent, Map<String, String> entities, 
                                             String message, Map<String, Double> intentConfidences) {
        double baseConfidence = intentConfidences.getOrDefault(intent, 0.0);
        
        // Boost confidence based on entities found
        double entityBoost = Math.min(0.3, entities.size() * 0.1);
        
        // Boost for message length and specificity
        double lengthBoost = Math.min(0.2, message.length() / 100.0);
        
        // Boost for multiple intent matches (indicates clear communication)
        long strongMatches = intentConfidences.values().stream()
            .mapToLong(score -> score > 0.5 ? 1 : 0)
            .sum();
        double clarityBoost = strongMatches > 1 ? 0.1 : 0.0;
        
        // Penalty for ambiguous messages
        double ambiguityPenalty = intentConfidences.size() > 3 ? -0.1 : 0.0;
        
        double finalConfidence = baseConfidence + entityBoost + lengthBoost + clarityBoost + ambiguityPenalty;
        return Math.max(0.0, Math.min(1.0, finalConfidence));
    }
    
    private boolean shouldHandoffToHuman(String intent, String sentiment, double confidence, UserContext context) {
        // Emergency situations always need human handoff
        if ("emergency".equals(intent)) {
            return true;
        }
        
        // Complaints with negative sentiment
        if ("complaint".equals(intent) && "negative".equals(sentiment)) {
            return true;
        }
        
        // Low confidence in understanding
        if (confidence < 0.4) {
            return true;
        }
        
        // Multiple failed attempts (based on context)
        if (context != null && context.getMessageCount() > 5 && "unknown".equals(intent)) {
            return true;
        }
        
        return false;
    }
    
    private String generateSuggestedResponse(String intent, Map<String, String> entities, 
                                           UserContext context, String sentiment) {
        // Handle emergency first
        if ("emergency".equals(intent)) {
            return getRandomTemplate("emergency_response");
        }
        
        // Handle complaints with empathy
        if ("complaint".equals(intent) || "negative".equals(sentiment)) {
            return "I understand your concern and I want to help resolve this issue. Let me connect you with a specialist who can assist you better.";
        }
        
        // Context-aware responses
        if (context != null && context.getCustomerName() != null) {
            if ("greeting".equals(intent)) {
                return getRandomTemplate("greeting_returning")
                    .replace("{name}", context.getCustomerName())
                    .replace("{vehicle}", getVehicleDisplayName(context.getVehicle()));
            }
        } else if ("greeting".equals(intent)) {
            return getRandomTemplate("greeting_new");
        }
        
        // Default response based on intent
        return generateDefaultResponse(intent, entities);
    }
    
    private String getRandomTemplate(String templateKey) {
        List<String> templates = responseTemplates.get(templateKey);
        if (templates != null && !templates.isEmpty()) {
            return templates.get(new Random().nextInt(templates.size()));
        }
        return "I'm here to help you with your automotive needs.";
    }
    
    private String generateDefaultResponse(String intent, Map<String, String> entities) {
        switch (intent) {
            case "provide_name":
                return "Thank you! Now, what vehicle do you need service for?";
            case "vehicle_info":
                return "Great! What type of service do you need for your vehicle?";
            case "service_inquiry":
                return "I can help you with that service. Would you like to schedule an appointment?";
            case "booking_request":
                return "I'd be happy to help you schedule an appointment. Let me check our availability.";
            case "confirmation":
                return "Perfect! I'll take care of that for you.";
            case "negation":
                return "No problem. Is there something else I can help you with?";
            default:
                return "I'm here to help with your automotive needs. Could you please tell me more about what you're looking for?";
        }
    }
    
    private String getVehicleDisplayName(VehicleDTO vehicle) {
        if (vehicle != null) {
            return vehicle.getDisplayName();
        }
        return "vehicle";
    }
}
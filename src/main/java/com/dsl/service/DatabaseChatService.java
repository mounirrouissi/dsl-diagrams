package com.dsl.service;

import com.dsl.controller.*;
import com.dsl.entity.*;
import com.dsl.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class DatabaseChatService {
@Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    @Autowired
    private CustomerRepository customerRepository;
@Autowired
    private VehicleRepository vehicleRepository;

@Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceHistoryRepository serviceHistoryRepository;
    

    @Autowired
    private EnhancedNLPService enhancedNLPService;

    public DatabaseChatService() {
    }
    
    public ChatMessageResponse processMessage(String sessionId, String userMessage, UserContext context) {
        // Use enhanced NLP service for processing
        NLPResult nlpResult = enhancedNLPService.processMessage(userMessage, context);
        
        // Generate response using the NLP result
        return generateResponse(sessionId, userMessage, nlpResult, context);
    }

    public ChatMessageResponse generateResponse(String sessionId, String userMessage, NLPResult nlpResult,
                                                UserContext context) {
        
        // Get or create chat session
        ChatSession session = getOrCreateSession(sessionId);
        
        // Save user message
        ChatMessage userMsg = new ChatMessage(session, userMessage, MessageSender.USER, nlpResult.getIntent());
        userMsg.setEntities(convertEntitiesToMap(nlpResult.getEntities()));
        userMsg.setConfidence(BigDecimal.valueOf(nlpResult.getConfidence()));
        chatMessageRepository.save(userMsg);
        
        // Update session context
        updateSessionContext(session, context);
        
        // Generate response based on intent and context
        String responseMessage;
        List<QuickReply> quickReplies = new ArrayList<>();
        UserContext updatedContext = mergeContextFromSession(session, context);
        
        String intent = nlpResult.getIntent();
        Map<String, String> entities = nlpResult.getEntities();
        
        switch (intent) {
            case "greeting":
                responseMessage = handleGreeting(updatedContext, entities, session);
                quickReplies = getGreetingQuickReplies();
                break;
                
            case "provide_name":
                responseMessage = handleNameProvision(updatedContext, entities, session);
                quickReplies = getVehicleInfoQuickReplies();
                break;
                
            case "vehicle_info":
                responseMessage = handleVehicleInfo(updatedContext, entities, session);
                quickReplies = getServiceQuickReplies();
                break;
                
            case "service_inquiry":
                responseMessage = handleServiceInquiry(updatedContext, entities, session);
                quickReplies = getBookingQuickReplies();
                break;
                
            case "booking_request":
                responseMessage = handleBookingRequest(updatedContext, entities, session);
                quickReplies = getTimeSlotQuickReplies();
                break;
                
            case "confirmation":
                responseMessage = handleConfirmation(updatedContext, entities, session);
                break;
                
            case "emergency":
                responseMessage = handleEmergency(updatedContext, entities, session, userMessage);
                quickReplies = getEmergencyQuickReplies();
                break;
                
            case "complaint":
                responseMessage = handleComplaint(updatedContext, entities, session);
                quickReplies = getComplaintQuickReplies();
                break;
                
            case "tire_assistance":
                responseMessage = handleTireAssistance(updatedContext, entities, session, userMessage);
                quickReplies = getTireAssistanceQuickReplies();
                break;
                
            case "oil_assistance":
                responseMessage = handleOilAssistance(updatedContext, entities, session, userMessage);
                quickReplies = getOilAssistanceQuickReplies();
                break;
                
            case "confirmation":
                responseMessage = handleConfirmation(updatedContext, entities, session);
                break;
                
            default:
                responseMessage = handleDefault(updatedContext, userMessage);
                quickReplies = getDefaultQuickReplies();
        }
        
        // Save bot response
        ChatMessage botMsg = new ChatMessage(session, responseMessage, MessageSender.BOT, intent);
        chatMessageRepository.save(botMsg);
        
        // Update session activity
        session.setLastActivity(LocalDateTime.now());
        chatSessionRepository.save(session);
        
        return new ChatMessageResponse(responseMessage, intent, updatedContext, quickReplies);
    }
    
    private ChatSession getOrCreateSession(String sessionId) {
        return chatSessionRepository.findBySessionId(sessionId)
            .orElseGet(() -> {
                ChatSession newSession = new ChatSession(sessionId);
                return chatSessionRepository.save(newSession);
            });
    }
    
    private void updateSessionContext(ChatSession session, UserContext context) {
        if (context != null) {
            Map<String, Object> contextData = session.getContextData();
            if (contextData == null) {
                contextData = new HashMap<>();
            }
            
            if (context.getCustomerName() != null) {
                contextData.put("customerName", context.getCustomerName());
            }
            if (context.getVehicle() != null) {
                contextData.put("vehicle", convertVehicleToMap(context.getVehicle()));
            }
            if (context.getCurrentService() != null) {
                contextData.put("currentService", context.getCurrentService());
            }
            if (context.getConversationState() != null) {
                contextData.put("conversationState", context.getConversationState());
            }
            
            session.setContextData(contextData);
        }
    }
    
    private UserContext mergeContextFromSession(ChatSession session, UserContext incomingContext) {
        UserContext context = incomingContext != null ? incomingContext : new UserContext();
        
        Map<String, Object> sessionData = session.getContextData();
        if (sessionData != null) {
            if (context.getCustomerName() == null && sessionData.containsKey("customerName")) {
                context.setCustomerName((String) sessionData.get("customerName"));
            }
            if (context.getVehicle() == null && sessionData.containsKey("vehicle")) {
                context.setVehicle(convertMapToVehicle((Map<String, Object>) sessionData.get("vehicle")));
            }
            if (context.getCurrentService() == null && sessionData.containsKey("currentService")) {
                context.setCurrentService((String) sessionData.get("currentService"));
            }
            if (context.getConversationState() == null && sessionData.containsKey("conversationState")) {
                context.setConversationState((String) sessionData.get("conversationState"));
            }
        }
        
        return context;
    }
    
    private String handleGreeting(UserContext context, Map<String, String> entities, ChatSession session) {
        // Check if we have an existing customer
        if (context.getCustomerId() != null) {
            Optional<Customer> customer = customerRepository.findById(context.getCustomerId());
            if (customer.isPresent()) {
                session.setCustomer(customer.get());
                return String.format("Welcome back, %s! How can I help you with your vehicle today?", 
                    customer.get().getFirstName());
            }
        }
        
        // Check if name was provided in greeting
        String name = entities.get("person_name");
        if (name != null) {
            // Try to find existing customer
            List<Customer> customers = customerRepository.searchByName(name);
            if (!customers.isEmpty()) {
                Customer customer = customers.get(0);
                session.setCustomer(customer);
                context.setCustomerId(customer.getId());
                context.setCustomerName(customer.getFullName());
                return String.format("Welcome back, %s! I see you have a %s. How can I help you today?", 
                    customer.getFirstName(), getCustomerVehicleInfo(customer));
            } else {
                context.setCustomerName(name);
                context.setConversationState("new_customer");
                return String.format("Nice to meet you, %s! I'm here to help with your automotive needs. What vehicle do you need service for?", name);
            }
        }
        
        return "Hello! I'm your auto service assistant. I can help you with vehicle maintenance, service bookings, and answer questions about your car. What's your name?";
    }
    
    private String handleNameProvision(UserContext context, Map<String, String> entities, ChatSession session) {
        String name = entities.get("person_name");
        if (name != null) {
            // Try to find existing customer
            List<Customer> customers = customerRepository.searchByName(name);
            if (!customers.isEmpty()) {
                Customer customer = customers.get(0);
                session.setCustomer(customer);
                context.setCustomerId(customer.getId());
                context.setCustomerName(customer.getFullName());
                
                List<Vehicle> vehicleDTOS = vehicleRepository.findByCustomerId(customer.getId());
                if (!vehicleDTOS.isEmpty()) {
                    Vehicle primaryVehicleDTO = vehicleDTOS.get(0);
                    context.setVehicle(convertEntityToDto(primaryVehicleDTO));
                    return String.format("Great to see you again, %s! I see you have a %s. What service do you need today?", 
                        customer.getFirstName(), primaryVehicleDTO.getDisplayName());
                }
                
                return String.format("Welcome back, %s! What vehicle do you need service for?", customer.getFirstName());
            } else {
                context.setCustomerName(name);
                context.setConversationState("new_customer");
                return String.format("Nice to meet you, %s! What vehicle do you need service for? Please tell me the make, model, and year.", name);
            }
        }
        return "I didn't catch your name. Could you please tell me your name again?";
    }
    
    private String handleVehicleInfo(UserContext context, Map<String, String> entities, ChatSession session) {
        String make = entities.get("vehicle_make");
        String model = entities.get("vehicle_model");
        String yearStr = entities.get("vehicle_year");
        
        if (make != null) {
            VehicleDTO vehicleDto = context.getVehicle();
            if (vehicleDto == null) {
                vehicleDto = new VehicleDTO();
                context.setVehicle(vehicleDto);
            }
            
            vehicleDto.setMake(make);
            if (model != null) vehicleDto.setModel(model);
            if (yearStr != null) {
                try {
                    vehicleDto.setYear(Integer.parseInt(yearStr));
                } catch (NumberFormatException e) {
                    // Handle invalid year
                }
            }
            
            // If we have a customer, try to find or create the vehicle
            if (session.getCustomer() != null) {
                List<Vehicle> existingVehicleDTOS = vehicleRepository.findByCustomerIdAndMake(
                    session.getCustomer().getId(), make);
                
                if (!existingVehicleDTOS.isEmpty()) {
                    Vehicle vehicleDTO = existingVehicleDTOS.get(0);
                    context.setVehicle(convertEntityToDto(vehicleDTO));
                    
                    // Get service recommendations
                    List<ServiceHistory> history = serviceHistoryRepository.findByVehicleIdOrderByServiceDateDesc(vehicleDTO.getId());
                    String recommendations = getServiceRecommendations(vehicleDTO, history);
                    
                    return String.format("Perfect! I have your %s on file. %s What service do you need today?", 
                        vehicleDTO.getDisplayName(), recommendations);
                }
            }
            
            context.setConversationState("vehicle_provided");
            return String.format("Great! I have your %s %s %s. What service do you need today?", 
                yearStr != null ? yearStr : "", make, model != null ? model : "");
        }
        
        return "I need a bit more information about your vehicle. Could you tell me the make, model, and year? For example: '2020 Honda Civic'";
    }
    
    private String handleServiceInquiry(UserContext context, Map<String, String> entities, ChatSession session) {
        String serviceType = entities.get("service_type");
        if (serviceType != null) {
            // Find matching service
            List<com.dsl.entity.Service> services = serviceRepository.searchByNameOrDescription(serviceType);
            if (!services.isEmpty()) {
                com.dsl.entity.Service service = services.get(0);
                context.setCurrentService(service.getName());
                
                return String.format("Perfect! I can help you with %s. This service typically takes about %d minutes and costs $%.2f. Would you like to schedule an appointment?", 
                    service.getName(), service.getDurationMinutes(), service.getPrice());
            }
            
            context.setCurrentService(serviceType);
            return String.format("I can help you with %s service. Would you like to schedule an appointment?", serviceType);
        }
        
        return "What type of service do you need? We offer oil changes, tire rotations, brake inspections, and more.";
    }
    
    private String handleBookingRequest(UserContext context, Map<String, String> entities, ChatSession session) {
        if (context.getVehicle() == null || context.getCurrentService() == null) {
            return "I'll need your vehicle information and the service you want before we can book an appointment. What car do you drive and what service do you need?";
        }
        
        // Check availability and suggest times
        List<String> availableSlots = getAvailableTimeSlots();
        context.setConversationState("booking_appointment");
        
        return String.format("I'd be happy to schedule your %s for your %s. Here are some available times: %s. Which works best for you?", 
            context.getCurrentService(), 
            getVehicleDisplayName(context.getVehicle()),
            String.join(", ", availableSlots.subList(0, Math.min(3, availableSlots.size()))));
    }
    
    private String handleConfirmation(UserContext context, Map<String, String> entities, ChatSession session) {
        String conversationState = context.getConversationState();
        
        if ("booking_appointment".equals(conversationState)) {
            // Create the appointment
            if (session.getCustomer() != null && context.getVehicle() != null && context.getCurrentService() != null) {
                try {
                    createAppointment(session.getCustomer(), context);
                    context.setConversationState("appointment_confirmed");
                    return String.format("Perfect! I've scheduled your %s appointment for your %s. You'll receive a confirmation shortly. Is there anything else I can help you with?",
                        context.getCurrentService(), getVehicleDisplayName(context.getVehicle()));
                } catch (Exception e) {
                    return "I'm sorry, there was an issue scheduling your appointment. Let me try again or you can call us directly.";
                }
            }
        } else if ("battery_issue_diagnosed".equals(conversationState)) {
            // User confirmed they want roadside assistance
            if (context.getCurrentLocation() != null) {
                context.setConversationState("roadside_dispatched");
                return String.format("Great! ✅ I've dispatched roadside assistance to %s. The nearest technician is about 15 minutes away. " +
                    "Would you like me to also check if your battery needs replacement while they're there?", 
                    context.getCurrentLocation());
            } else {
                return "I'll be happy to send roadside assistance. Can you please share your current location?";
            }
        } else if ("roadside_dispatched".equals(conversationState)) {
            // User confirmed they want battery check
            context.setConversationState("full_service_scheduled");
            return "Perfect! I've noted that for a battery replacement check. You'll receive a text update when the technician is on the way. " +
                   "For your safety, please stay inside your vehicle with hazard lights on. 🚨 " +
                   "I'll stay connected and update you every 5 minutes until help arrives.";
        }
        
        return "Thank you for confirming! Is there anything else I can help you with today?";
    }
    
    private String handleEmergency(UserContext context, Map<String, String> entities, ChatSession session, String userMessage) {
        context.setConversationState("emergency_mode");
        context.setEmergencyMode(true);
        
        // Analyze the emergency type and symptoms
        String emergencyType = analyzeEmergencyType(userMessage);
        String location = entities.get("location");
        String symptom = entities.get("car_symptom");
        
        StringBuilder response = new StringBuilder();
        
        if (emergencyType.equals("wont_start")) {
            response.append("I'm sorry to hear that your car won't start and you're stuck. Let me help you right away. ");
            
            // Check for specific symptoms mentioned
            if (symptom != null && symptom.toLowerCase().contains("click")) {
                response.append("You mentioned it clicks - that usually indicates a weak or dead battery. ");
                context.setConversationState("battery_issue_diagnosed");
                context.setEmergencyType("battery_dead");
            } else if (userMessage.toLowerCase().contains("click")) {
                response.append("Thank you, that clicking sound usually indicates a weak or dead battery. ");
                context.setConversationState("battery_issue_diagnosed");
                context.setEmergencyType("battery_dead");
            } else {
                response.append("Could you tell me if the car makes any sound when you try to start it? ");
                context.setConversationState("diagnosing_startup_issue");
            }
        } else if (emergencyType.equals("breakdown")) {
            response.append("I understand you're having car trouble and are stranded. I'm here to help get you back on the road safely. ");
            context.setConversationState("diagnosing_breakdown");
        } else {
            response.append("I can see this is urgent. Let me get you the help you need right away. ");
        }
        
        // Handle location information
        if (location != null) {
            response.append(String.format("Got it ✅. I've pinned your location at %s. ", location));
            context.setCurrentLocation(location);
            
            if ("battery_issue_diagnosed".equals(context.getConversationState())) {
                response.append("The nearest technician is about 15 minutes away. Would you like me to also check if your battery needs replacement while they're there?");
                context.setConversationState("roadside_dispatched");
            }
        } else if ("battery_issue_diagnosed".equals(context.getConversationState())) {
            response.append("I can send roadside assistance to jump-start your car. Can you share your current location so I can dispatch help?");
        }
        
        return response.toString();
    }
    
    private String handleComplaint(UserContext context, Map<String, String> entities, ChatSession session) {
        context.setConversationState("handling_complaint");
        
        return "I understand your concern and I sincerely apologize for any inconvenience. Your satisfaction is very important to us. " +
               "Let me connect you with a specialist who can address this issue properly and make things right. " +
               "In the meantime, could you please provide more details about what happened?";
    }
    
    private String handleTireAssistance(UserContext context, Map<String, String> entities, ChatSession session, String userMessage) {
        context.setConversationState("tire_assistance");
        context.setCurrentService("tire_assistance");
        
        String tireIssue = entities.get("tire_issue");
        String carProblem = entities.get("car_problem");
        String location = entities.get("location");
        
        StringBuilder response = new StringBuilder();
        
        // Analyze the specific tire problem
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("flat") || lowerMessage.contains("wheel")) {
            response.append("Got it — sounds like you have a flat tire. ");
            context.setEmergencyType("flat_tire");
            
            if (lowerMessage.contains("nail") || lowerMessage.contains("hit")) {
                response.append("You mentioned hitting something - that's likely a puncture. ");
                context.setConversationState("punctured_tire");
            }
            
            if (lowerMessage.contains("can't move") || lowerMessage.contains("won't move")) {
                response.append("Since your car can't move, I'll prioritize this as urgent. ");
                context.setEmergencyMode(true);
            }
            
            response.append("Do you have a spare tire available, or would you like us to bring one?");
            
        } else if (lowerMessage.contains("nail")) {
            response.append("Understood. That's a punctured tire from road debris. ");
            response.append("I can send a technician to replace it or inflate it temporarily. Where are you located?");
            context.setConversationState("punctured_tire");
            
        } else if (lowerMessage.contains("fix") || lowerMessage.contains("repair")) {
            response.append("Yes, I can arrange tire assistance. ");
            response.append("Could you confirm if the tire needs a full replacement or just air?");
            context.setConversationState("tire_diagnosis_needed");
            
        } else {
            response.append("I can help with your tire issue. ");
            response.append("Could you tell me more about what's wrong with the tire?");
            context.setConversationState("tire_diagnosis_needed");
        }
        
        // Handle location if provided
        if (location != null) {
            context.setCurrentLocation(location);
            response.append(String.format(" I have your location as %s.", location));
        }
        
        return response.toString();
    }
    
    private String handleOilAssistance(UserContext context, Map<String, String> entities, ChatSession session, String userMessage) {
        context.setConversationState("oil_assistance");
        context.setCurrentService("oil_assistance");
        
        String oilIssue = entities.get("oil_issue");
        String location = entities.get("location");
        
        StringBuilder response = new StringBuilder();
        
        // Analyze the specific oil problem
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("oil light")) {
            response.append("Thanks for letting me know. That oil light means your engine oil is low. ");
            response.append("I can send oil delivery and refill service. Would you like me to arrange that now?");
            context.setConversationState("oil_light_on");
            
        } else if (lowerMessage.contains("out of oil") || lowerMessage.contains("empty")) {
            response.append("Of course. I'll send a technician with engine oil to top up your car so you can drive safely again. ");
            response.append("Can you share your location?");
            context.setConversationState("oil_empty");
            context.setEmergencyMode(true); // Empty oil is urgent
            
        } else if (lowerMessage.contains("engine fluids")) {
            response.append("Yes, we provide oil assistance. If your oil is low or empty, we can deliver and refill it for you. ");
            response.append("Is that what you need right now?");
            context.setConversationState("oil_service_inquiry");
            
        } else if (lowerMessage.contains("low oil")) {
            response.append("I can help with low oil. We'll send someone to check your oil level and top it up if needed. ");
            response.append("Where are you located?");
            context.setConversationState("oil_low");
            
        } else {
            response.append("I can help with oil-related services. ");
            response.append("Could you tell me more about the oil issue you're experiencing?");
            context.setConversationState("oil_diagnosis_needed");
        }
        
        // Handle location if provided
        if (location != null) {
            context.setCurrentLocation(location);
            response.append(String.format(" I have your location as %s.", location));
        }
        
        return response.toString();
    }
    
    private String handleConfirmation(UserContext context, Map<String, String> entities, ChatSession session) {
        String conversationState = context.getConversationState();
        
        if ("booking_appointment".equals(conversationState)) {
            // Create the appointment
            if (session.getCustomer() != null && context.getVehicle() != null && context.getCurrentService() != null) {
                try {
                    createAppointment(session.getCustomer(), context);
                    context.setConversationState("appointment_confirmed");
                    return String.format("Perfect! I've scheduled your %s appointment for your %s. You'll receive a confirmation shortly. Is there anything else I can help you with?",
                        context.getCurrentService(), getVehicleDisplayName(context.getVehicle()));
                } catch (Exception e) {
                    return "I'm sorry, there was an issue scheduling your appointment. Let me try again or you can call us directly.";
                }
            }
        } else if ("battery_issue_diagnosed".equals(conversationState)) {
            // User confirmed they want roadside assistance
            if (context.getCurrentLocation() != null) {
                context.setConversationState("roadside_dispatched");
                return String.format("Great! ✅ I've dispatched roadside assistance to %s. The nearest technician is about 15 minutes away. " +
                    "Would you like me to also check if your battery needs replacement while they're there?", 
                    context.getCurrentLocation());
            } else {
                return "I'll be happy to send roadside assistance. Can you please share your current location?";
            }
        } else if ("roadside_dispatched".equals(conversationState)) {
            // User confirmed they want battery check
            context.setConversationState("full_service_scheduled");
            return "Perfect! I've noted that for a battery replacement check. You'll receive a text update when the technician is on the way. " +
                   "For your safety, please stay inside your vehicle with hazard lights on. 🚨 " +
                   "I'll stay connected and update you every 5 minutes until help arrives.";
        } else if ("tire_assistance".equals(conversationState) || "punctured_tire".equals(conversationState)) {
            // User confirmed tire service
            if (context.getCurrentLocation() != null) {
                context.setConversationState("tire_service_dispatched");
                return String.format("Excellent! I've dispatched a tire technician to %s. They'll arrive in about 20-30 minutes with the necessary equipment. " +
                    "Please stay safe and keep your hazard lights on if you're on the roadside. 🚨", context.getCurrentLocation());
            } else {
                return "Great! I'll arrange tire assistance. Can you please share your current location so I can dispatch help?";
            }
        } else if ("oil_assistance".equals(conversationState) || "oil_light_on".equals(conversationState) || "oil_empty".equals(conversationState)) {
            // User confirmed oil service
            if (context.getCurrentLocation() != null) {
                context.setConversationState("oil_service_dispatched");
                String urgencyNote = "oil_empty".equals(conversationState) ? 
                    "This is urgent since you're out of oil. " : "";
                return String.format("Perfect! %sI've arranged oil service to %s. A technician will arrive in 15-25 minutes with fresh engine oil. " +
                    "Please avoid driving until they arrive to prevent engine damage.", urgencyNote, context.getCurrentLocation());
            } else {
                return "Understood! I'll send oil assistance. Can you please share your location so I can dispatch a technician?";
            }
        }
        
        return "Thank you for confirming! Is there anything else I can help you with today?";
    }
    
    private String analyzeEmergencyType(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("won't start") || lowerMessage.contains("wont start") || 
            lowerMessage.contains("doesn't start") || lowerMessage.contains("not starting")) {
            return "wont_start";
        } else if (lowerMessage.contains("broke down") || lowerMessage.contains("broken down") ||
                   lowerMessage.contains("stuck") || lowerMessage.contains("stranded")) {
            return "breakdown";
        } else if (lowerMessage.contains("accident") || lowerMessage.contains("crash")) {
            return "accident";
        } else if (lowerMessage.contains("smoke") || lowerMessage.contains("fire") || 
                   lowerMessage.contains("burning")) {
            return "fire_hazard";
        }
        
        return "general_emergency";
    }
    
    private String handleDefault(UserContext context) {
        String conversationState = context.getConversationState();
        
        // Emergency mode responses
        if ("diagnosing_startup_issue".equals(conversationState)) {
            return "Please let me know what sounds your car makes when you try to start it - this will help me diagnose the issue better.";
        } else if ("battery_issue_diagnosed".equals(conversationState)) {
            if (context.getCurrentLocation() != null) {
                return "I'm ready to send roadside assistance for a jump-start to " + context.getCurrentLocation() + 
                       ". Should I dispatch them now?";
            } else {
                return "I'm ready to send roadside assistance for a jump-start. I just need your location to dispatch help.";
            }
        } else if ("roadside_dispatched".equals(conversationState)) {
            return "Your roadside assistance is on the way! I'll keep you updated. In the meantime, please stay safe in your vehicle with hazard lights on. 🚨";
        } else if ("full_service_scheduled".equals(conversationState)) {
            return "Everything is set up! The technician will be there soon and will also check your battery. I'll update you every 5 minutes until they arrive.";
        } else if ("emergency_mode".equals(conversationState)) {
            return "I'm still here to help with your emergency. What additional information can you provide?";
        }
        
        // Handle location-only messages during emergency
        if (context.isEmergencyMode() && context.getCurrentLocation() == null) {
            // Check if the message might be a location
            String location = extractLocationFromMessage(userMessage);
            if (location != null) {
                context.setCurrentLocation(location);
                if ("battery_issue_diagnosed".equals(conversationState)) {
                    return String.format("Got it ✅. I've pinned your location at %s. The nearest technician is about 15 minutes away. " +
                        "Would you like me to also check if your battery needs replacement while they're there?", location);
                }
            }
            return handlePotentialLocation(context);
        }
        
        return "I'm here to help with your automotive needs. You can ask me about services, schedule appointments, or get information about vehicle maintenance. What would you like to know?";
    }
    
    private String extractLocationFromMessage(String message) {
        // Simple location extraction - look for common location patterns
        String lowerMessage = message.toLowerCase();
        
        // Check for "I'm at/near/on" patterns
        if (lowerMessage.matches(".*\\b(at|near|on)\\s+.*")) {
            // Extract everything after the preposition
            String[] parts = message.split("\\b(?:at|near|on)\\s+", 2);
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        
        // Check for street/location keywords
        if (lowerMessage.contains("street") || lowerMessage.contains("avenue") || 
            lowerMessage.contains("road") || lowerMessage.contains("station") ||
            lowerMessage.contains("mall") || lowerMessage.contains("center")) {
            return message.trim();
        }
        
        return null;
    }
    
    private String handlePotentialLocation(UserContext context) {
        return "Got it! Let me confirm - are you saying that's your current location? If so, I can dispatch roadside assistance right away.";
    }
    
    // Helper methods
    private String getCustomerVehicleInfo(Customer customer) {
        List<Vehicle> vehicleDTOS = vehicleRepository.findByCustomerId(customer.getId());
        if (!vehicleDTOS.isEmpty()) {
            return vehicleDTOS.get(0).getDisplayName();
        }
        return "vehicle";
    }
    
    private String getServiceRecommendations(Vehicle vehicleDTO, List<ServiceHistory> history) {
        if (history.isEmpty()) {
            return "I don't see any recent service history.";
        }
        
        ServiceHistory lastService = history.get(0);
        return String.format("Your last service was %s on %s.", 
            lastService.getService().getName(), lastService.getServiceDate());
    }
    
    private List<String> getAvailableTimeSlots() {
        // Simplified - in real implementation, check actual availability
        return Arrays.asList("Tomorrow 9:00 AM", "Tomorrow 2:00 PM", "Friday 10:00 AM", "Friday 3:00 PM");
    }
    
    private void createAppointment(Customer customer, UserContext context) {
        // Find or create vehicle
        Vehicle vehicleDTO = findOrCreateVehicle(customer, context.getVehicle());
        
        // Find service
        Optional<com.dsl.entity.Service> serviceOpt = serviceRepository.findByNameIgnoreCase(context.getCurrentService());
        if (serviceOpt.isPresent()) {
            Appointment appointment = new Appointment(customer, vehicleDTO, serviceOpt.get(), LocalDateTime.now().plusDays(1));
            appointmentRepository.save(appointment);
        }
    }
    
    private Vehicle findOrCreateVehicle(Customer customer, VehicleDTO vehicleDto) {
        if (vehicleDto.getMake() != null && vehicleDto.getModel() != null && vehicleDto.getYear() != null) {
            List<Vehicle> existing = vehicleRepository.findByCustomerIdAndMake(customer.getId(), vehicleDto.getMake());
            if (!existing.isEmpty()) {
                return existing.get(0);
            }
            
            Vehicle newVehicleDTO = new Vehicle(customer, vehicleDto.getMake(), vehicleDto.getModel(), vehicleDto.getYear());
            return vehicleRepository.save(newVehicleDTO);
        }
        return null;
    }
    
    private String getVehicleDisplayName(VehicleDTO vehicle) {
        if (vehicle.getYear() != null && vehicle.getMake() != null && vehicle.getModel() != null) {
            return vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel();
        }
        return "vehicle";
    }
    
    // Quick reply methods
    private List<QuickReply> getGreetingQuickReplies() {
        return Arrays.asList(
            new QuickReply("I need service for my car"),
            new QuickReply("Book an appointment"),
            new QuickReply("Check service history")
        );
    }
    
    private List<QuickReply> getVehicleInfoQuickReplies() {
        return Arrays.asList(
            new QuickReply("2020 Honda Civic"),
            new QuickReply("2019 Toyota Camry"),
            new QuickReply("2021 Ford F-150")
        );
    }
    
    private List<QuickReply> getServiceQuickReplies() {
        return Arrays.asList(
            new QuickReply("Oil change"),
            new QuickReply("Tire rotation"),
            new QuickReply("Brake inspection"),
            new QuickReply("General maintenance")
        );
    }
    
    private List<QuickReply> getBookingQuickReplies() {
        return Arrays.asList(
            new QuickReply("Yes, schedule appointment"),
            new QuickReply("Tell me more about the service"),
            new QuickReply("What's the cost?")
        );
    }
    
    private List<QuickReply> getTimeSlotQuickReplies() {
        return Arrays.asList(
            new QuickReply("Tomorrow morning"),
            new QuickReply("This afternoon"),
            new QuickReply("Next week")
        );
    }
    
    private List<QuickReply> getEmergencyQuickReplies() {
        return Arrays.asList(
            new QuickReply("It makes clicking sounds"),
            new QuickReply("No sound at all"),
            new QuickReply("Engine cranks but won't start"),
            new QuickReply("Send roadside assistance")
        );
    }
    
    private List<QuickReply> getComplaintQuickReplies() {
        return Arrays.asList(
            new QuickReply("Speak to manager"),
            new QuickReply("File formal complaint"),
            new QuickReply("Request refund"),
            new QuickReply("Schedule callback")
        );
    }
    
    private List<QuickReply> getTireAssistanceQuickReplies() {
        return Arrays.asList(
            new QuickReply("I have a spare tire"),
            new QuickReply("Please bring a tire"),
            new QuickReply("Just need air/inflation"),
            new QuickReply("Send technician now")
        );
    }
    
    private List<QuickReply> getOilAssistanceQuickReplies() {
        return Arrays.asList(
            new QuickReply("Yes, arrange oil service"),
            new QuickReply("Check oil level first"),
            new QuickReply("Emergency oil delivery"),
            new QuickReply("Schedule oil change")
        );
    }
    
    private List<QuickReply> getDefaultQuickReplies() {
        return Arrays.asList(
            new QuickReply("I need car service"),
            new QuickReply("Book appointment"),
            new QuickReply("Emergency help"),
            new QuickReply("Talk to human")
        );
    }
    
    // Conversion methods
    private Map<String, Object> convertEntitiesToMap(Map<String, String> entities) {
        return new HashMap<>(entities);
    }
    
    private Map<String, Object> convertVehicleToMap(VehicleDTO vehicleDTO) {
        Map<String, Object> map = new HashMap<>();
        if (vehicleDTO.getMake() != null) map.put("make", vehicleDTO.getMake());
        if (vehicleDTO.getModel() != null) map.put("model", vehicleDTO.getModel());
        if (vehicleDTO.getYear() != null) map.put("year", vehicleDTO.getYear());
        return map;
    }
    
    private VehicleDTO convertMapToVehicle(Map<String, Object> map) {
        VehicleDTO vehicleDTO = new VehicleDTO();
        if (map.containsKey("make")) vehicleDTO.setMake((String) map.get("make"));
        if (map.containsKey("model")) vehicleDTO.setModel((String) map.get("model"));
        if (map.containsKey("year")) vehicleDTO.setYear((Integer) map.get("year"));
        return vehicleDTO;
    }
    
    private VehicleDTO convertEntityToDto(Vehicle entity) {
        VehicleDTO dto = new VehicleDTO();
        dto.setMake(entity.getMake());
        dto.setModel(entity.getModel());
        dto.setYear(entity.getYear());
        dto.setColor(entity.getColor());
        dto.setVin(entity.getVin());
        dto.setMileage(entity.getMileage());
        return dto;
    }
    
    // Public methods for chat history
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestamp(sessionId);
    }
    
    public UserContext updateContext(String sessionId, UserContext context) {
        ChatSession session = getOrCreateSession(sessionId);
        updateSessionContext(session, context);
        chatSessionRepository.save(session);
        return context;
    }
}
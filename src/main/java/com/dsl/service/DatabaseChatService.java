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
    

    private NLPService nlpService;

    public DatabaseChatService() {
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
                
            default:
                responseMessage = handleDefault(updatedContext);
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
        if ("booking_appointment".equals(context.getConversationState())) {
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
        }
        
        return "Thank you for confirming! Is there anything else I can help you with today?";
    }
    
    private String handleDefault(UserContext context) {
        return "I'm here to help with your automotive needs. You can ask me about services, schedule appointments, or get information about vehicle maintenance. What would you like to know?";
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
    
    private List<QuickReply> getDefaultQuickReplies() {
        return Arrays.asList(
            new QuickReply("I need car service"),
            new QuickReply("Book appointment"),
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
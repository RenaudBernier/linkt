package com.linkt.controller;

import com.linkt.dto.StudentRegistrationDTO;
import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.model.User;
import com.linkt.repository.EventRepository;
import com.linkt.service.EventService;
import com.linkt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all registered students for a specific event
     * Only the organizer who created the event can view this
     */
    @GetMapping("/{eventId}/registered-students")
    public ResponseEntity<?> getRegisteredStudents(
            @PathVariable Long eventId,
            Authentication authentication) {

        try {
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            // Get authenticated user
            User user = (User) authentication.getPrincipal();

            // Check if user is an organizer
            if (!(user instanceof Organizer)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Only organizers can access this resource");
            }

            Organizer organizer = (Organizer) user;

            // Get registered students
            List<StudentRegistrationDTO> students = eventService.getRegisteredStudentsForEvent(eventId, organizer);
            return ResponseEntity.ok(students);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Event not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Event not found");
            } else if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have permission to view this event's students");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/organizer")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<List<java.util.Map<String, Object>>> getOrganizerEvents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Event> events = eventRepository.findByOrganizerUserId(user.getUserId());
        
        // Transform events to include ticket count
        List<java.util.Map<String, Object>> eventsWithTicketCount = events.stream()
            .map(event -> {
                java.util.Map<String, Object> eventMap = new java.util.HashMap<>();
                eventMap.put("eventId", event.getEventId());
                eventMap.put("title", event.getTitle());
                eventMap.put("description", event.getDescription());
                eventMap.put("eventType", event.getEventType());
                eventMap.put("startDateTime", event.getStartDateTime());
                eventMap.put("endDateTime", event.getEndDateTime());
                eventMap.put("location", event.getLocation());
                eventMap.put("capacity", event.getCapacity());
                eventMap.put("imageUrl", event.getImageUrl());
                eventMap.put("price", event.getPrice());
                eventMap.put("ticketCount", event.getTickets().size());
                return eventMap;
            })
            .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(eventsWithTicketCount);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Event> addEvent(@RequestBody com.linkt.dto.EventDTO eventDTO, Authentication authentication)
    {
        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username)
                                        .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!(currentUser instanceof com.linkt.model.Organizer)) {
            throw new RuntimeException("Authenticated user is not an Organizer");
        }
        com.linkt.model.Organizer organizer = (com.linkt.model.Organizer) currentUser;

        Event event = new Event();
        event.setTitle(eventDTO.getTitle());  
        event.setDescription(eventDTO.getDescription());
        event.setEventType(eventDTO.getEventType());
        event.setPrice(eventDTO.getPrice());
        event.setStartDateTime(eventDTO.getStartDateTime());
        event.setEndDateTime(eventDTO.getEndDateTime());
        event.setLocation(eventDTO.getLocation()); 
        event.setCapacity(eventDTO.getCapacity());
        event.setImageUrl(eventDTO.getImage());
        event.setOrganizer(organizer);
        
        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.status(201).body(savedEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody com.linkt.dto.EventDTO eventDTO, Authentication authentication) {
        // Get the authenticated organizer
        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!(currentUser instanceof com.linkt.model.Organizer)) {
            return ResponseEntity.status(403).body("Authenticated user is not an Organizer");
        }
        com.linkt.model.Organizer organizer = (com.linkt.model.Organizer) currentUser;

        // Fetch the existing event
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Verify ownership - only the event owner can edit
        if (!event.getOrganizer().getUserId().equals(organizer.getUserId())) {
            return ResponseEntity.status(403).body("You don't have permission to edit this event");
        }

        // Validate capacity against existing ticket count
        int ticketCount = event.getTickets().size();
        if (eventDTO.getCapacity() < ticketCount) {
            return ResponseEntity.status(400).body("Capacity cannot be reduced below " + ticketCount + " (current number of sold tickets)");
        }

        // Update event fields
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setEventType(eventDTO.getEventType());
        event.setPrice(eventDTO.getPrice());
        event.setStartDateTime(eventDTO.getStartDateTime());
        event.setEndDateTime(eventDTO.getEndDateTime());
        event.setLocation(eventDTO.getLocation());
        event.setCapacity(eventDTO.getCapacity());
        if (eventDTO.getImage() != null && !eventDTO.getImage().isEmpty()) {
            event.setImageUrl(eventDTO.getImage());
        }

        // Save updated event
        Event updatedEvent = eventRepository.save(event);
        return ResponseEntity.ok(updatedEvent);
    }
}

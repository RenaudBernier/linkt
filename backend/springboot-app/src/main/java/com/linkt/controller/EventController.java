package com.linkt.controller;

import com.linkt.dto.StudentRegistrationDTO;
import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.model.User;
import com.linkt.repository.EventRepository;
import com.linkt.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService eventService;

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
}

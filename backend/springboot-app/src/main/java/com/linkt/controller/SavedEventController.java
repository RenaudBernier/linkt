package com.linkt.controller;

import com.linkt.model.Event;
import com.linkt.model.SavedEvent;
import com.linkt.model.Student;
import com.linkt.repository.EventRepository;
import com.linkt.repository.SavedEventRepository;
import com.linkt.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-events")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SavedEventController {

    @Autowired
    private SavedEventRepository savedEventRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping
    public ResponseEntity<?> saveEvent(@RequestBody Map<String, Long> request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            Long eventId = request.get("eventId");

            if (eventId == null) {
                return ResponseEntity.badRequest().body("Event ID is required");
            }

            Student student = studentRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Check if already saved
            if (savedEventRepository.findByStudent_UserIdAndEvent_EventId(student.getUserId(), eventId).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Event already saved");
            }

            SavedEvent savedEvent = new SavedEvent();
            savedEvent.setStudent(student);
            savedEvent.setEvent(event);

            SavedEvent saved = savedEventRepository.save(savedEvent);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving event: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getSavedEventsByUser(@PathVariable Long userId) {
        try {
            List<SavedEvent> savedEvents = savedEventRepository.findByStudent_UserId(userId);
            List<Event> events = savedEvents.stream()
                    .map(SavedEvent::getEvent)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching saved events: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMySavedEvents(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            Student student = studentRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<SavedEvent> savedEvents = savedEventRepository.findByStudent_UserId(student.getUserId());
            List<Event> events = savedEvents.stream()
                    .map(SavedEvent::getEvent)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching saved events: " + e.getMessage());
        }
    }

    @GetMapping("/check/{eventId}")
    public ResponseEntity<?> checkIfSaved(@PathVariable Long eventId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            Student student = studentRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            boolean isSaved = savedEventRepository.findByStudent_UserIdAndEvent_EventId(
                    student.getUserId(), eventId).isPresent();

            return ResponseEntity.ok(Map.of("isSaved", isSaved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking saved status: " + e.getMessage());
        }
    }

    @DeleteMapping("/event/{eventId}")
    @Transactional
    public ResponseEntity<?> unsaveEvent(@PathVariable Long eventId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            Student student = studentRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            savedEventRepository.deleteByStudent_UserIdAndEvent_EventId(student.getUserId(), eventId);

            return ResponseEntity.ok(Map.of("message", "Event unsaved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error unsaving event: " + e.getMessage());
        }
    }
}

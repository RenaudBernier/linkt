
package com.linkt.controller;

import com.linkt.dto.AdministratorDTO;
import com.linkt.dto.EventResponseDTO;
import com.linkt.dto.GlobalStatsResponse;
import com.linkt.dto.OrganizerResponseDTO;
import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.repository.EventRepository;
import com.linkt.repository.OrganizerRepository;
import com.linkt.service.AdministratorService;
import com.linkt.service.GlobalStatsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/administrators")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdministratorController {

    // Approve an event
    @PostMapping("/events/{eventId}/approve")
    public ResponseEntity<String> approveEvent(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
        }
        event.setStatus("approved");
        eventRepository.save(event);
        return new ResponseEntity<>("Event approved", HttpStatus.OK);
    }

    // Reject an event
    @PostMapping("/events/{eventId}/reject")
    public ResponseEntity<String> rejectEvent(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
        }
        event.setStatus("rejected");
        eventRepository.save(event);
        return new ResponseEntity<>("Event rejected", HttpStatus.OK);
    }

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private GlobalStatsService globalStatsService;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private EventRepository eventRepository;

    // Get global statistics (admin only)
    @GetMapping("/stats/global")
    public ResponseEntity<GlobalStatsResponse> getGlobalStatistics() {
        try {
            GlobalStatsResponse stats = globalStatsService.getGlobalStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create administrator (promote user to admin)
    @PostMapping
    public ResponseEntity<AdministratorDTO> createAdministrator(@Valid @RequestBody AdministratorDTO dto) {
        try {
            AdministratorDTO created = administratorService.createAdministrator(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all administrators
    @GetMapping
    public ResponseEntity<List<AdministratorDTO>> getAllAdministrators() {
        List<AdministratorDTO> admins = administratorService.getAllAdministrators();
        return new ResponseEntity<>(admins, HttpStatus.OK);
    }

    // Get administrator by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdministratorDTO> getAdministratorById(@PathVariable Long id) {
        try {
            AdministratorDTO admin = administratorService.getAdministratorById(id);
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Get administrator by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<AdministratorDTO> getAdministratorByUserId(@PathVariable Long userId) {
        try {
            AdministratorDTO admin = administratorService.getAdministratorByUserId(userId);
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Check if user is an administrator
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> isAdministrator(@PathVariable Long userId) {
        boolean isAdmin = administratorService.isAdministrator(userId);
        return new ResponseEntity<>(isAdmin, HttpStatus.OK);
    }

    // Delete administrator (remove admin privileges)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdministrator(@PathVariable Long id) {
        try {
            administratorService.deleteAdministrator(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Get all organizers
    @GetMapping("/organizers")
    public ResponseEntity<List<OrganizerResponseDTO>> getAllOrganizers() {
        try {
            List<Organizer> organizers = organizerRepository.findAll();
            List<OrganizerResponseDTO> response = organizers.stream()
                    .map(organizer -> new OrganizerResponseDTO(
                            organizer.getUserId(),
                            organizer.getFirstName(),
                            organizer.getLastName(),
                            organizer.getEmail(),
                            organizer.getOrganizationName(),
                            organizer.getPhoneNumber(),
                            organizer.isApproved() ? "approved" : "pending"
                    ))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all events
    @GetMapping("/events")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        try {
            List<Event> events = eventRepository.findAll();
            List<EventResponseDTO> response = events.stream()
                    .map(event -> {
                        int ticketCount = event.getTickets() != null ? event.getTickets().size() : 0;
                        int scannedCount = event.getTickets() != null
                            ? (int) event.getTickets().stream().filter(ticket -> ticket.getIsScanned() != null && ticket.getIsScanned()).count()
                            : 0;

                        return new EventResponseDTO(
                            event.getEventId(),
                            event.getTitle(),
                            event.getDescription(),
                            event.getEventType(),
                            event.getLocation(),
                            event.getStartDateTime(),
                            event.getEndDateTime(),
                            event.getCapacity(),
                            event.getPrice(),
                            ticketCount,
                            scannedCount,
                            event.getOrganizer() != null ? event.getOrganizer().getUserId() : null
                        );
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
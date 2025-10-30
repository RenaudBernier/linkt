package com.linkt.controller;

import com.linkt.dto.ScanRequest;
import com.linkt.dto.ScanResponse;
import com.linkt.dto.ScanStatsResponse;
import com.linkt.model.Event;
import com.linkt.model.Student;
import com.linkt.model.Ticket;
import com.linkt.model.User;
import com.linkt.repository.EventRepository;
import com.linkt.repository.StudentRepository;
import com.linkt.repository.TicketRepository;
import com.linkt.repository.UserRepository;
import com.linkt.service.TicketScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketScanService ticketScanService;

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Map<String, Long> request, Authentication authentication) {
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

            Ticket ticket = new Ticket();
            ticket.setStudent(student);
            ticket.setEvent(event);

            // Save to get ticketId, then generate and update QR code
            ticket = ticketRepository.saveAndFlush(ticket);
            ticket.generateQRCode();
            ticket = ticketRepository.save(ticket);

            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating ticket: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getUserTickets(@PathVariable Long userId) {
        List<Ticket> tickets = ticketRepository.findByStudent_UserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            Student student = studentRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            List<Ticket> tickets = ticketRepository.findByStudent_UserId(student.getUserId());
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching tickets: " + e.getMessage());
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicket(@PathVariable Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> ResponseEntity.ok(ticket))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/events/{eventId}/validate")
    public ResponseEntity<?> validateTicket(
            @PathVariable Long eventId,
            @RequestBody ScanRequest request,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ScanResponse response = ticketScanService.validateTicket(
                    request.getQrCode(),
                    eventId,
                    user.getUserId()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ScanResponse(false, e.getMessage(), "ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ScanResponse(false, "Error validating ticket: " + e.getMessage(), "ERROR"));
        }
    }

    @GetMapping("/events/{eventId}/scan-stats")
    public ResponseEntity<?> getScanStats(
            @PathVariable Long eventId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ScanStatsResponse stats = ticketScanService.getScanStats(eventId, user.getUserId());
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching scan stats: " + e.getMessage());
        }
    }
}

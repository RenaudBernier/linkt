package com.linkt.controller;

import com.linkt.model.Event;
import com.linkt.model.Student;
import com.linkt.model.Ticket;
import com.linkt.repository.EventRepository;
import com.linkt.repository.StudentRepository;
import com.linkt.repository.TicketRepository;
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
            ticket.generateQRCode();

            Ticket savedTicket = ticketRepository.save(ticket);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedTicket);
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
}

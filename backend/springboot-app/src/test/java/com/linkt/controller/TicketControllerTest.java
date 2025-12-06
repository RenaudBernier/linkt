package com.linkt.controller;

import com.linkt.linkt.LinktApplication;
import com.linkt.model.*;
import com.linkt.repository.EventRepository;
import com.linkt.repository.StudentRepository;
import com.linkt.repository.TicketRepository;
import com.linkt.repository.UserRepository;
import com.linkt.service.TicketScanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TicketController - Calendar Feature
 * User Story: "As a student, I want to save events to a calendar"
 *
 * These tests verify that ticket/event data contains all fields
 * required for .ics calendar file generation.
 */
@SpringBootTest(classes = LinktApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketRepository ticketRepository;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TicketScanService ticketScanService;

    // Helper methods to create test data
    private Student createTestStudent() {
        Student student = new Student("student@example.com", "John", "Doe", "1234567890", "hashedPassword");
        student.setUserId(1L);
        student.setEmailVerified(true);
        return student;
    }

    private Event createTestEvent() {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "9876543210", "hashedPassword");
        organizer.setUserId(2L);

        Event event = new Event("Tech Conference 2025", "Annual technology conference", "conference",
                "2025-06-15T09:00:00", "2025-06-15T17:00:00", "Montreal Convention Center", 500);
        event.setEventId(1L);
        event.setOrganizer(organizer);
        return event;
    }

    private Ticket createTestTicket(Student student, Event event) {
        Ticket ticket = new Ticket(student, event);
        ticket.setTicketId(1L);
        ticket.setQrCode("LINKT-1-1");
        return ticket;
    }

    // Calendar Feature Tests

    @Test
    @DisplayName("GET /api/tickets/{id} should return ticket with all calendar-required event fields")
    void shouldReturnTicketWithCalendarEventFields() throws Exception {
        Student student = createTestStudent();
        Event event = createTestEvent();
        Ticket ticket = createTestTicket(student, event);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // Verify all fields required for .ics calendar generation are present
        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.qrCode").exists())
                .andExpect(jsonPath("$.event.title").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.event.description").value("Annual technology conference"))
                .andExpect(jsonPath("$.event.startDateTime").value("2025-06-15T09:00:00"))
                .andExpect(jsonPath("$.event.endDateTime").value("2025-06-15T17:00:00"))
                .andExpect(jsonPath("$.event.location").value("Montreal Convention Center"));
    }

    @Test
    @DisplayName("GET /api/tickets/{id} should return 404 for non-existent ticket")
    void shouldReturn404ForNonExistentTicket() throws Exception {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tickets/user/{userId} should return all tickets for calendar sync")
    void shouldReturnAllUserTicketsForCalendarSync() throws Exception {
        Student student = createTestStudent();

        // Create multiple events for calendar
        Event event1 = createTestEvent();

        Organizer organizer2 = new Organizer("org2@example.com", "Bob", "Jones", "5555555555", "hashedPassword");
        organizer2.setUserId(3L);
        Event event2 = new Event("Music Festival", "Summer music festival", "music",
                "2025-07-20T14:00:00", "2025-07-20T23:00:00", "Olympic Stadium", 10000);
        event2.setEventId(2L);
        event2.setOrganizer(organizer2);

        Ticket ticket1 = createTestTicket(student, event1);
        Ticket ticket2 = new Ticket(student, event2);
        ticket2.setTicketId(2L);
        ticket2.setQrCode("LINKT-2-1");

        when(ticketRepository.findByStudent_UserId(1L)).thenReturn(Arrays.asList(ticket1, ticket2));

        mockMvc.perform(get("/api/tickets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].event.title").value("Tech Conference 2025"))
                .andExpect(jsonPath("$[0].event.startDateTime").exists())
                .andExpect(jsonPath("$[0].event.endDateTime").exists())
                .andExpect(jsonPath("$[0].event.location").exists())
                .andExpect(jsonPath("$[1].event.title").value("Music Festival"))
                .andExpect(jsonPath("$[1].event.startDateTime").exists())
                .andExpect(jsonPath("$[1].event.endDateTime").exists())
                .andExpect(jsonPath("$[1].event.location").exists());
    }

    @Test
    @DisplayName("GET /api/tickets/user/{userId} should return empty list when no tickets exist")
    void shouldReturnEmptyListWhenNoTickets() throws Exception {
        when(ticketRepository.findByStudent_UserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tickets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}

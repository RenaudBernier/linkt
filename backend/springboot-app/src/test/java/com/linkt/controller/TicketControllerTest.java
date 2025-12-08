package com.linkt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkt.dto.ScanRequest;
import com.linkt.dto.ScanResponse;
import com.linkt.dto.ScanStatsResponse;
import com.linkt.dto.TicketData;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TicketController
 *
 * User Stories Covered:
 * - U1: "As a student, I want to save events to a calendar"
 * - U2: "As a Student, I want to buy tickets"
 * - U3: "As a Student, I want to view digital tickets"
 * - U11: "As an Organizer, I want to scan QR tickets"
 *
 * These tests verify:
 * - Ticket purchasing flow (POST /api/tickets)
 * - QR code generation in format LINKT-{eventId}-{ticketId}
 * - Ticket retrieval endpoints
 * - Calendar-required fields for .ics generation
 * - QR code scanning and validation (POST /api/tickets/events/{eventId}/validate)
 * - Scan statistics (GET /api/tickets/events/{eventId}/scan-stats)
 */
@SpringBootTest(classes = LinktApplication.class)
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser
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
    @WithMockUser
    void shouldReturn404ForNonExistentTicket() throws Exception {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tickets/user/{userId} should return all tickets for calendar sync")
    @WithMockUser
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
    @WithMockUser
    void shouldReturnEmptyListWhenNoTickets() throws Exception {
        when(ticketRepository.findByStudent_UserId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tickets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== U3: Digital Ticket Display Tests ====================

    @Test
    @DisplayName("U3: GET /api/tickets/{id} should return digital ticket with QR code")
    @WithMockUser(username = "student@example.com")
    void shouldReturnDigitalTicketWithQRCode() throws Exception {
        Student student = createTestStudent();
        Event event = createTestEvent();
        Ticket ticket = createTestTicket(student, event);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(1))
                .andExpect(jsonPath("$.qrCode").value("LINKT-1-1"))
                .andExpect(jsonPath("$.qrCode").isNotEmpty())
                .andExpect(jsonPath("$.student.firstName").value("John"))
                .andExpect(jsonPath("$.student.lastName").value("Doe"))
                .andExpect(jsonPath("$.event.title").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.event.location").value("Montreal Convention Center"))
                .andExpect(jsonPath("$.event.startDateTime").value("2025-06-15T09:00:00"));
    }

    @Test
    @DisplayName("U3: GET /api/tickets/me should return list of user's digital tickets with QR codes")
    @WithMockUser(username = "student@example.com")
    void shouldReturnMyDigitalTicketsWithQRCodes() throws Exception {
        Student student = createTestStudent();
        Event event1 = createTestEvent();
        Event event2 = new Event("Music Festival", "Summer music festival", "music",
                "2025-07-20T14:00:00", "2025-07-20T23:00:00", "Olympic Stadium", 10000);
        event2.setEventId(2L);

        Ticket ticket1 = createTestTicket(student, event1);
        Ticket ticket2 = new Ticket(student, event2);
        ticket2.setTicketId(2L);
        ticket2.setQrCode("LINKT-2-2");

        when(studentRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(ticketRepository.findByStudent_UserId(1L)).thenReturn(Arrays.asList(ticket1, ticket2));

        mockMvc.perform(get("/api/tickets/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticketId").value(1))
                .andExpect(jsonPath("$[0].qrCode").value("LINKT-1-1"))
                .andExpect(jsonPath("$[0].qrCode").isNotEmpty())
                .andExpect(jsonPath("$[1].ticketId").value(2))
                .andExpect(jsonPath("$[1].qrCode").value("LINKT-2-2"))
                .andExpect(jsonPath("$[1].qrCode").isNotEmpty());
    }

    @Test
    @DisplayName("U3: QR code should be unique for each ticket")
    void shouldGenerateUniqueQRCodeForEachTicket() {
        Student student = createTestStudent();
        Event event = createTestEvent();

        Ticket ticket1 = new Ticket(student, event);
        ticket1.setTicketId(1L);
        String qrCode1 = ticket1.generateQRCode();

        Ticket ticket2 = new Ticket(student, event);
        ticket2.setTicketId(2L);
        String qrCode2 = ticket2.generateQRCode();

        // Different ticket IDs should produce different QR codes
        assert !qrCode1.equals(qrCode2) : "QR codes should be unique for different tickets";
        assert qrCode1.equals("LINKT-1-1") : "First QR code should be LINKT-1-1";
        assert qrCode2.equals("LINKT-1-2") : "Second QR code should be LINKT-1-2";
    }

    @Test
    @DisplayName("U3: QR code should contain event and ticket identifiers")
    void shouldIncludeEventAndTicketIdentifiersInQRCode() {
        Student student = createTestStudent();
        Event event = createTestEvent();
        event.setEventId(42L);

        Ticket ticket = new Ticket(student, event);
        ticket.setTicketId(99L);

        String qrCode = ticket.generateQRCode();

        assert qrCode.contains("42") : "QR code should contain event ID";
        assert qrCode.contains("99") : "QR code should contain ticket ID";
        assert qrCode.equals("LINKT-42-99") : "QR code should follow LINKT-{eventId}-{ticketId} format";
    }

    @Test
    @DisplayName("U3: Digital ticket should show scan status")
    @WithMockUser
    void shouldShowTicketScanStatus() throws Exception {
        Student student = createTestStudent();
        Event event = createTestEvent();
        Ticket ticket = createTestTicket(student, event);

        // Initially not scanned
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isScanned").value(false))
                .andExpect(jsonPath("$.scannedAt").doesNotExist())
                .andExpect(jsonPath("$.scannedBy").doesNotExist());
    }

    @Test
    @DisplayName("U3: Digital ticket should show scan details when scanned")
    @WithMockUser
    void shouldShowScanDetailsWhenTicketIsScanned() throws Exception {
        Student student = createTestStudent();
        Event event = createTestEvent();
        Organizer organizer = (Organizer) event.getOrganizer();

        Ticket ticket = createTestTicket(student, event);
        ticket.markAsScanned(organizer);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isScanned").value(true))
                .andExpect(jsonPath("$.scannedAt").exists())
                .andExpect(jsonPath("$.scannedBy").exists())
                .andExpect(jsonPath("$.scannedBy.firstName").value("Jane"))
                .andExpect(jsonPath("$.scannedBy.lastName").value("Smith"));
    }

    @Test
    @DisplayName("U3: Should not return ticket belonging to different user")
    @WithMockUser(username = "different@example.com")
    void shouldNotReturnOtherUserTicket() throws Exception {
        Student student1 = createTestStudent();
        Student student2 = new Student("different@example.com", "Jane", "Smith", "9999999999", "hashedPassword");
        student2.setUserId(2L);

        Event event = createTestEvent();
        Ticket ticket = createTestTicket(student1, event);

        when(studentRepository.findByEmail("different@example.com")).thenReturn(Optional.of(student2));
        when(ticketRepository.findByStudent_UserId(2L)).thenReturn(Collections.emptyList());

        // Different user should not see student1's ticket in their list
        mockMvc.perform(get("/api/tickets/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ==================== U2: Ticket Purchasing Tests ====================

    @Test
    @DisplayName("POST /api/tickets should create ticket with QR code for authenticated student")
    @WithMockUser(username = "student@example.com")
    void shouldCreateTicketWithQRCode() throws Exception {
        Student student = createTestStudent();
        Event event = createTestEvent();

        // Create ticket that will be returned after save
        Ticket savedTicket = new Ticket(student, event);
        savedTicket.setTicketId(42L);
        savedTicket.setQrCode("LINKT-1-42");

        when(studentRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        Map<String, Long> request = new HashMap<>();
        request.put("eventId", 1L);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").value(42))
                .andExpect(jsonPath("$.qrCode").value("LINKT-1-42"))
                .andExpect(jsonPath("$.student").exists())
                .andExpect(jsonPath("$.event").exists())
                .andExpect(jsonPath("$.event.eventId").value(1))
                .andExpect(jsonPath("$.event.title").value("Tech Conference 2025"));
    }

    @Test
    @DisplayName("POST /api/tickets should return 403 for unauthenticated user")
    void shouldReturn403ForUnauthenticatedTicketPurchase() throws Exception {
        Map<String, Long> request = new HashMap<>();
        request.put("eventId", 1L);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/tickets should return 400 when eventId is missing")
    @WithMockUser(username = "student@example.com")
    void shouldReturn400WhenEventIdMissing() throws Exception {
        Map<String, Long> request = new HashMap<>();
        // No eventId provided

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tickets should return 500 when event not found")
    @WithMockUser(username = "student@example.com")
    void shouldReturn500WhenEventNotFound() throws Exception {
        Student student = createTestStudent();

        when(studentRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Long> request = new HashMap<>();
        request.put("eventId", 999L);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/tickets should return 500 when student not found")
    @WithMockUser(username = "unknown@example.com")
    void shouldReturn500WhenStudentNotFound() throws Exception {
        when(studentRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Map<String, Long> request = new HashMap<>();
        request.put("eventId", 1L);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/tickets/me should return authenticated user's tickets")
    @WithMockUser(username = "student@example.com")
    void shouldReturnAuthenticatedUserTickets() throws Exception {
        Student student = createTestStudent();
        Event event = createTestEvent();
        Ticket ticket = createTestTicket(student, event);

        when(studentRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(ticketRepository.findByStudent_UserId(1L)).thenReturn(Arrays.asList(ticket));

        mockMvc.perform(get("/api/tickets/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticketId").value(1))
                .andExpect(jsonPath("$[0].qrCode").value("LINKT-1-1"))
                .andExpect(jsonPath("$[0].event.title").value("Tech Conference 2025"));
    }

    @Test
    @DisplayName("GET /api/tickets/me should return 403 for unauthenticated user")
    void shouldReturn403ForUnauthenticatedGetMyTickets() throws Exception {
        mockMvc.perform(get("/api/tickets/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("QR code should follow format LINKT-{eventId}-{ticketId}")
    void shouldGenerateQRCodeInCorrectFormat() {
        Student student = createTestStudent();
        Event event = createTestEvent();

        Ticket ticket = new Ticket(student, event);
        ticket.setTicketId(42L);

        String qrCode = ticket.generateQRCode();

        // Verify format: LINKT-{eventId}-{ticketId}
        assert qrCode != null : "QR code should not be null";
        assert qrCode.equals("LINKT-1-42") : "QR code format should be LINKT-{eventId}-{ticketId}";
        assert qrCode.startsWith("LINKT-") : "QR code should start with LINKT-";
        assert qrCode.split("-").length == 3 : "QR code should have 3 parts separated by dashes";
    }

    @Test
    @DisplayName("Ticket should validate its own QR code correctly")
    void shouldValidateQRCodeCorrectly() {
        Student student = createTestStudent();
        Event event = createTestEvent();

        Ticket ticket = new Ticket(student, event);
        ticket.setTicketId(42L);
        ticket.generateQRCode();

        // Valid QR code
        assert ticket.validateQRCode("LINKT-1-42") : "Should validate correct QR code";

        // Invalid QR codes
        assert !ticket.validateQRCode("LINKT-1-99") : "Should reject wrong ticket ID";
        assert !ticket.validateQRCode("LINKT-2-42") : "Should reject wrong event ID";
        assert !ticket.validateQRCode("INVALID-CODE") : "Should reject invalid format";
        assert !ticket.validateQRCode(null) : "Should reject null QR code";
    }

    @Test
    @DisplayName("New ticket should have isScanned set to false")
    void newTicketShouldNotBeScanned() {
        Student student = createTestStudent();
        Event event = createTestEvent();

        Ticket ticket = new Ticket(student, event);

        assert ticket.getIsScanned() == false : "New ticket should not be scanned";
        assert ticket.getScannedAt() == null : "New ticket should not have scannedAt";
        assert ticket.getScannedBy() == null : "New ticket should not have scannedBy";
    }

    @Test
    @DisplayName("Ticket markAsScanned should set all scan fields")
    void markAsScannedShouldSetAllFields() {
        Student student = createTestStudent();
        Event event = createTestEvent();
        Organizer organizer = (Organizer) event.getOrganizer();

        Ticket ticket = new Ticket(student, event);
        ticket.markAsScanned(organizer);

        assert ticket.getIsScanned() == true : "isScanned should be true";
        assert ticket.getScannedAt() != null : "scannedAt should be set";
        assert ticket.getScannedBy() == organizer : "scannedBy should be the organizer";
    }

    // ==================== U11: QR Code Scanning Tests ====================

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should scan valid QR code")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldScanValidQRCode() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);
        Event event = createTestEvent();
        Student student = createTestStudent();

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-1-42");

        TicketData ticketData = new TicketData(
                42L, "John Doe", "student@example.com", "Tech Conference 2025",
                "2025-06-15T09:00:00", "General Admission"
        );
        ScanResponse scanResponse = new ScanResponse(true, "Ticket validated successfully", "SUCCESS");
        scanResponse.setTicketData(ticketData);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.validateTicket("LINKT-1-42", 1L, 2L)).thenReturn(scanResponse);

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Ticket validated successfully"))
                .andExpect(jsonPath("$.ticketData.ticketId").value(42))
                .andExpect(jsonPath("$.ticketData.studentName").value("John Doe"))
                .andExpect(jsonPath("$.ticketData.eventName").value("Tech Conference 2025"));
    }

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should reject invalid QR code")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldRejectInvalidQRCode() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("INVALID-QR-CODE");

        ScanResponse scanResponse = new ScanResponse(false, "Invalid ticket code", "INVALID");

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.validateTicket("INVALID-QR-CODE", 1L, 2L)).thenReturn(scanResponse);

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("INVALID"))
                .andExpect(jsonPath("$.message").value("Invalid ticket code"));
    }

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should reject already scanned ticket")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldRejectAlreadyScannedTicket() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-1-42");

        ScanResponse scanResponse = new ScanResponse(false, "Ticket already scanned", "ALREADY_SCANNED");
        scanResponse.setScannedAt("2025-06-15T10:30:00");
        scanResponse.setScannedBy("Jane Smith");

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.validateTicket("LINKT-1-42", 1L, 2L)).thenReturn(scanResponse);

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("ALREADY_SCANNED"))
                .andExpect(jsonPath("$.message").value("Ticket already scanned"))
                .andExpect(jsonPath("$.scannedAt").value("2025-06-15T10:30:00"))
                .andExpect(jsonPath("$.scannedBy").value("Jane Smith"));
    }

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should reject ticket for wrong event")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldRejectTicketForWrongEvent() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-2-42");

        ScanResponse scanResponse = new ScanResponse(false,
                "This ticket is for a different event: Music Festival", "WRONG_EVENT");

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.validateTicket("LINKT-2-42", 1L, 2L)).thenReturn(scanResponse);

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("WRONG_EVENT"))
                .andExpect(jsonPath("$.message").value("This ticket is for a different event: Music Festival"));
    }

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should return 401 for unauthenticated user")
    void shouldReturn401ForUnauthenticatedScan() throws Exception {
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-1-42");

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should return 403 for unauthorized organizer")
    @WithMockUser(username = "other-organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturn403ForUnauthorizedOrganizerScan() throws Exception {
        Organizer otherOrganizer = new Organizer("other-organizer@example.com", "Bob", "Jones", "9999999999", "hashedPassword");
        otherOrganizer.setUserId(3L);

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-1-42");

        when(userRepository.findByEmail("other-organizer@example.com")).thenReturn(Optional.of(otherOrganizer));
        when(ticketScanService.validateTicket("LINKT-1-42", 1L, 3L))
                .thenThrow(new RuntimeException("Unauthorized: You can only scan tickets for your own events"));

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("U11: POST /api/tickets/events/{eventId}/validate should return 403 for student attempting to scan")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldReturn403ForStudentAttemptingToScan() throws Exception {
        Student student = createTestStudent();

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-1-42");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));
        when(ticketScanService.validateTicket("LINKT-1-42", 1L, 1L))
                .thenThrow(new RuntimeException("Only organizers can scan tickets"));

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U11: GET /api/tickets/events/{eventId}/scan-stats should return scan statistics")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturnScanStatistics() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);

        ScanStatsResponse statsResponse = new ScanStatsResponse(
                1L, "Tech Conference 2025", 100, 75, 25
        );

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.getScanStats(1L, 2L)).thenReturn(statsResponse);

        mockMvc.perform(get("/api/tickets/events/1/scan-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.eventName").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.totalTickets").value(100))
                .andExpect(jsonPath("$.scannedCount").value(75))
                .andExpect(jsonPath("$.remainingCount").value(25));
    }

    @Test
    @DisplayName("U11: GET /api/tickets/events/{eventId}/scan-stats should return zero counts for event with no tickets")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturnZeroStatsForEventWithNoTickets() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);

        ScanStatsResponse statsResponse = new ScanStatsResponse(
                1L, "Empty Event", 0, 0, 0
        );

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.getScanStats(1L, 2L)).thenReturn(statsResponse);

        mockMvc.perform(get("/api/tickets/events/1/scan-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").value(0))
                .andExpect(jsonPath("$.scannedCount").value(0))
                .andExpect(jsonPath("$.remainingCount").value(0));
    }

    @Test
    @DisplayName("U11: GET /api/tickets/events/{eventId}/scan-stats should return 401 for unauthenticated user")
    void shouldReturn401ForUnauthenticatedStats() throws Exception {
        mockMvc.perform(get("/api/tickets/events/1/scan-stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U11: GET /api/tickets/events/{eventId}/scan-stats should return 403 for unauthorized organizer")
    @WithMockUser(username = "other-organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturn403ForUnauthorizedOrganizerStats() throws Exception {
        Organizer otherOrganizer = new Organizer("other-organizer@example.com", "Bob", "Jones", "9999999999", "hashedPassword");
        otherOrganizer.setUserId(3L);

        when(userRepository.findByEmail("other-organizer@example.com")).thenReturn(Optional.of(otherOrganizer));
        when(ticketScanService.getScanStats(1L, 3L))
                .thenThrow(new RuntimeException("Unauthorized: You can only view stats for your own events"));

        mockMvc.perform(get("/api/tickets/events/1/scan-stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U11: Scan validation should handle concurrent scan attempts")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleConcurrentScanAttempts() throws Exception {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);

        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrCode("LINKT-1-42");

        // First scan succeeds
        TicketData ticketData = new TicketData(
                42L, "John Doe", "student@example.com", "Tech Conference 2025",
                "2025-06-15T09:00:00", "General Admission"
        );
        ScanResponse successResponse = new ScanResponse(true, "Ticket validated successfully", "SUCCESS");
        successResponse.setTicketData(ticketData);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(ticketScanService.validateTicket("LINKT-1-42", 1L, 2L))
                .thenReturn(successResponse);

        // First scan
        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Second scan attempt (should be rejected as already scanned)
        ScanResponse alreadyScannedResponse = new ScanResponse(false, "Ticket already scanned", "ALREADY_SCANNED");
        when(ticketScanService.validateTicket("LINKT-1-42", 1L, 2L))
                .thenReturn(alreadyScannedResponse);

        mockMvc.perform(post("/api/tickets/events/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("ALREADY_SCANNED"));
    }
}

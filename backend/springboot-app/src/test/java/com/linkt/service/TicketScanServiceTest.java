package com.linkt.service;

import com.linkt.dto.ScanResponse;
import com.linkt.dto.ScanStatsResponse;
import com.linkt.model.*;
import com.linkt.repository.EventRepository;
import com.linkt.repository.TicketRepository;
import com.linkt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketScanService
 *
 * User Story U2: "As a Student, I want to buy tickets"
 *
 * These tests verify:
 * - Ticket validation during scanning
 * - Duplicate scan prevention
 * - Wrong event detection
 * - Organizer authorization
 * - Scan statistics
 */
@ExtendWith(MockitoExtension.class)
class TicketScanServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketScanService ticketScanService;

    private Student testStudent;
    private Organizer testOrganizer;
    private Event testEvent;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Create test student
        testStudent = new Student("student@example.com", "John", "Doe", "1234567890", "hashedPassword");
        testStudent.setUserId(1L);

        // Create test organizer
        testOrganizer = new Organizer("organizer@example.com", "Jane", "Smith", "9876543210", "hashedPassword");
        testOrganizer.setUserId(2L);

        // Create test event
        testEvent = new Event("Tech Conference 2025", "Annual technology conference", "conference",
                "2025-06-15T09:00:00", "2025-06-15T17:00:00", "Montreal Convention Center", 500);
        testEvent.setEventId(1L);
        testEvent.setOrganizer(testOrganizer);

        // Create test ticket
        testTicket = new Ticket(testStudent, testEvent);
        testTicket.setTicketId(42L);
        testTicket.setQrCode("LINKT-1-42");
        testTicket.setIsScanned(false);
    }

    // ==================== Ticket Validation Tests ====================

    @Test
    @DisplayName("validateTicket should return SUCCESS for valid unscanned ticket")
    void shouldReturnSuccessForValidTicket() {
        when(ticketRepository.findByQrCode("LINKT-1-42")).thenReturn(Optional.of(testTicket));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testOrganizer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        ScanResponse response = ticketScanService.validateTicket("LINKT-1-42", 1L, 2L);

        assertTrue(response.isValid());
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getTicketData());
        assertEquals("John Doe", response.getTicketData().getStudentName());
        assertEquals("student@example.com", response.getTicketData().getStudentEmail());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("validateTicket should return INVALID for non-existent QR code")
    void shouldReturnInvalidForNonExistentQRCode() {
        when(ticketRepository.findByQrCode("LINKT-999-999")).thenReturn(Optional.empty());

        ScanResponse response = ticketScanService.validateTicket("LINKT-999-999", 1L, 2L);

        assertFalse(response.isValid());
        assertEquals("INVALID", response.getStatus());
        assertEquals("Invalid ticket code", response.getMessage());
    }

    @Test
    @DisplayName("validateTicket should return WRONG_EVENT when ticket belongs to different event")
    void shouldReturnWrongEventForMismatchedEvent() {
        // Create a different event
        Event differentEvent = new Event("Music Festival", "Summer festival", "music",
                "2025-07-20T14:00:00", "2025-07-20T23:00:00", "Stadium", 10000);
        differentEvent.setEventId(2L);
        differentEvent.setOrganizer(testOrganizer);

        // Ticket belongs to differentEvent
        Ticket ticketForDifferentEvent = new Ticket(testStudent, differentEvent);
        ticketForDifferentEvent.setTicketId(99L);
        ticketForDifferentEvent.setQrCode("LINKT-2-99");

        when(ticketRepository.findByQrCode("LINKT-2-99")).thenReturn(Optional.of(ticketForDifferentEvent));

        // Try to scan at event 1 instead of event 2
        ScanResponse response = ticketScanService.validateTicket("LINKT-2-99", 1L, 2L);

        assertFalse(response.isValid());
        assertEquals("WRONG_EVENT", response.getStatus());
        assertTrue(response.getMessage().contains("different event"));
        assertTrue(response.getMessage().contains("Music Festival"));
    }

    @Test
    @DisplayName("validateTicket should return ALREADY_SCANNED for previously scanned ticket")
    void shouldReturnAlreadyScannedForScannedTicket() {
        testTicket.setIsScanned(true);
        testTicket.setScannedAt("2025-06-15T10:30:00");
        testTicket.setScannedBy(testOrganizer);

        when(ticketRepository.findByQrCode("LINKT-1-42")).thenReturn(Optional.of(testTicket));

        ScanResponse response = ticketScanService.validateTicket("LINKT-1-42", 1L, 2L);

        assertFalse(response.isValid());
        assertEquals("ALREADY_SCANNED", response.getStatus());
        assertTrue(response.getMessage().contains("already scanned"));
        assertNotNull(response.getScannedAt());
        assertEquals("Jane Smith", response.getScannedBy());
    }

    @Test
    @DisplayName("validateTicket should throw exception when organizer is not authorized")
    void shouldThrowExceptionForUnauthorizedOrganizer() {
        // Create a different organizer
        Organizer unauthorizedOrganizer = new Organizer("other@example.com", "Bob", "Jones", "5555555555", "hashedPassword");
        unauthorizedOrganizer.setUserId(3L);

        when(ticketRepository.findByQrCode("LINKT-1-42")).thenReturn(Optional.of(testTicket));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Organizer with ID 3 tries to scan ticket for event organized by ID 2
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketScanService.validateTicket("LINKT-1-42", 1L, 3L);
        });

        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    @DisplayName("validateTicket should throw exception when event not found")
    void shouldThrowExceptionWhenEventNotFound() {
        when(ticketRepository.findByQrCode("LINKT-1-42")).thenReturn(Optional.of(testTicket));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketScanService.validateTicket("LINKT-1-42", 1L, 2L);
        });

        assertTrue(exception.getMessage().contains("Event not found"));
    }

    // ==================== Scan Statistics Tests ====================

    @Test
    @DisplayName("getScanStats should return correct statistics for event")
    void shouldReturnCorrectScanStats() {
        // Create multiple tickets
        Ticket ticket1 = new Ticket(testStudent, testEvent);
        ticket1.setTicketId(1L);
        ticket1.setIsScanned(true);

        Ticket ticket2 = new Ticket(testStudent, testEvent);
        ticket2.setTicketId(2L);
        ticket2.setIsScanned(false);

        Ticket ticket3 = new Ticket(testStudent, testEvent);
        ticket3.setTicketId(3L);
        ticket3.setIsScanned(true);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEvent_EventId(1L)).thenReturn(Arrays.asList(ticket1, ticket2, ticket3));
        when(ticketRepository.countByEvent_EventIdAndIsScanned(1L, true)).thenReturn(2L);

        ScanStatsResponse stats = ticketScanService.getScanStats(1L, 2L);

        assertEquals(1L, stats.getEventId());
        assertEquals("Tech Conference 2025", stats.getEventName());
        assertEquals(3, stats.getTotalTickets());
        assertEquals(2, stats.getScannedCount());
        assertEquals(1, stats.getRemainingCount());
    }

    @Test
    @DisplayName("getScanStats should return zero counts for event with no tickets")
    void shouldReturnZeroCountsForEventWithNoTickets() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEvent_EventId(1L)).thenReturn(Arrays.asList());
        when(ticketRepository.countByEvent_EventIdAndIsScanned(1L, true)).thenReturn(0L);

        ScanStatsResponse stats = ticketScanService.getScanStats(1L, 2L);

        assertEquals(0, stats.getTotalTickets());
        assertEquals(0, stats.getScannedCount());
        assertEquals(0, stats.getRemainingCount());
    }

    @Test
    @DisplayName("getScanStats should throw exception for unauthorized organizer")
    void getScanStatsShouldThrowExceptionForUnauthorizedOrganizer() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Organizer with ID 3 tries to get stats for event organized by ID 2
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketScanService.getScanStats(1L, 3L);
        });

        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    @DisplayName("getScanStats should throw exception when event not found")
    void getScanStatsShouldThrowExceptionWhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketScanService.getScanStats(999L, 2L);
        });

        assertTrue(exception.getMessage().contains("Event not found"));
    }
}

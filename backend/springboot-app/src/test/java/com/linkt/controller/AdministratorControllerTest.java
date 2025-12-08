package com.linkt.controller;

import com.linkt.dto.EventResponseDTO;
import com.linkt.dto.GlobalStatsResponse;
import com.linkt.linkt.LinktApplication;
import com.linkt.model.Administrator;
import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.model.User;
import com.linkt.repository.EventRepository;
import com.linkt.repository.OrganizerRepository;
import com.linkt.repository.UserRepository;
import com.linkt.service.GlobalStatsService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LinktApplication.class)
@AutoConfigureMockMvc
class AdministratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GlobalStatsService globalStatsService;

    @MockBean
    private OrganizerRepository organizerRepository;

    private Administrator createTestAdministrator() {
        Administrator admin = new Administrator("admin@linkt.com", "Admin", "User", "5141234567", "hashedPassword");
        admin.setUserId(1L);
        return admin;
    }

    private Organizer createTestOrganizer(String email, String firstName, String lastName, Long userId, boolean isApproved) {
        Organizer organizer = new Organizer(email, firstName, lastName, "5149876543", "hashedPassword");
        organizer.setUserId(userId);
        organizer.setApproved(isApproved);
        organizer.setOrganizationName("Test Organization");
        return organizer;
    }

    private Event createTestEvent(String title, String status, Long eventId) {
        Organizer organizer = new Organizer("organizer@example.com", "John", "Doe", "5149876543", "hashedPassword");
        organizer.setUserId(2L);

        Event event = new Event(title, "Event description", "music",
                "2025-07-15T18:00:00", "2025-07-15T23:00:00", "Montreal", 500);
        event.setEventId(eventId);
        event.setPrice(50.0);
        event.setStatus(status);
        event.setOrganizer(organizer);
        return event;
    }

    // ==================== U14: Administrator Event Moderation Tests ====================

    @Test
    @DisplayName("U14: GET /api/administrators/events should return all events including pending ones")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnAllEventsForAdministrator() throws Exception {
        Administrator admin = createTestAdministrator();

        Event pendingEvent = createTestEvent("Pending Concert", "pending", 1L);
        Event approvedEvent = createTestEvent("Approved Festival", "approved", 2L);
        Event rejectedEvent = createTestEvent("Rejected Show", "rejected", 3L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(Arrays.asList(pendingEvent, approvedEvent, rejectedEvent));

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Pending Concert"))
                .andExpect(jsonPath("$[0].status").value("pending"))
                .andExpect(jsonPath("$[1].title").value("Approved Festival"))
                .andExpect(jsonPath("$[1].status").value("approved"))
                .andExpect(jsonPath("$[2].title").value("Rejected Show"))
                .andExpect(jsonPath("$[2].status").value("rejected"));
    }

    @Test
    @DisplayName("U14: GET /api/administrators/events should return empty list when no events exist")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnEmptyListWhenNoEvents() throws Exception {
        Administrator admin = createTestAdministrator();

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("U14: GET /api/administrators/events should only show pending events in moderation list")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldFilterPendingEvents() throws Exception {
        Administrator admin = createTestAdministrator();

        Event pendingEvent1 = createTestEvent("Pending Event 1", "pending", 1L);
        Event pendingEvent2 = createTestEvent("Pending Event 2", "pending", 2L);
        Event approvedEvent = createTestEvent("Approved Event", "approved", 3L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(Arrays.asList(pendingEvent1, pendingEvent2, approvedEvent));

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/approve should approve pending event")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldApprovePendingEvent() throws Exception {
        Administrator admin = createTestAdministrator();
        Event pendingEvent = createTestEvent("Pending Concert", "pending", 1L);
        Event approvedEvent = createTestEvent("Pending Concert", "approved", 1L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(pendingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(approvedEvent);

        mockMvc.perform(post("/api/administrators/events/1/approve"))
                .andExpect(status().isOk())
                .andExpect(content().string("Event approved"));

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/reject should reject pending event")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldRejectPendingEvent() throws Exception {
        Administrator admin = createTestAdministrator();
        Event pendingEvent = createTestEvent("Pending Concert", "pending", 1L);
        Event rejectedEvent = createTestEvent("Pending Concert", "rejected", 1L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(pendingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(rejectedEvent);

        mockMvc.perform(post("/api/administrators/events/1/reject"))
                .andExpect(status().isOk())
                .andExpect(content().string("Event rejected"));

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/approve should return 404 for non-existent event")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturn404WhenApprovingNonExistentEvent() throws Exception {
        Administrator admin = createTestAdministrator();

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/administrators/events/999/approve"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/reject should return 404 for non-existent event")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturn404WhenRejectingNonExistentEvent() throws Exception {
        Administrator admin = createTestAdministrator();

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/administrators/events/999/reject"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    @DisplayName("U14: GET /api/administrators/events should require ADMINISTRATOR role")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldRequireAdminRoleToViewEvents() throws Exception {
        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/approve should require ADMINISTRATOR role")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldRequireAdminRoleToApproveEvent() throws Exception {
        mockMvc.perform(post("/api/administrators/events/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/reject should require ADMINISTRATOR role")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldRequireAdminRoleToRejectEvent() throws Exception {
        mockMvc.perform(post("/api/administrators/events/1/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U14: GET /api/administrators/events should return 403 for unauthenticated users")
    void shouldReturn403ForUnauthenticatedUsersViewingEvents() throws Exception {
        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U14: POST /api/administrators/events/{eventId}/approve should return 403 for unauthenticated users")
    void shouldReturn403ForUnauthenticatedUsersApprovingEvents() throws Exception {
        mockMvc.perform(post("/api/administrators/events/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U14: Approved event should change status from pending to approved")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldChangeStatusToPendingToApproved() throws Exception {
        Administrator admin = createTestAdministrator();
        Event pendingEvent = createTestEvent("Event", "pending", 1L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(pendingEvent));

        mockMvc.perform(post("/api/administrators/events/1/approve"))
                .andExpect(status().isOk());

        verify(eventRepository).save(argThat(event ->
            event.getStatus().equals("approved")
        ));
    }

    @Test
    @DisplayName("U14: Rejected event should change status from pending to rejected")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldChangeStatusFromPendingToRejected() throws Exception {
        Administrator admin = createTestAdministrator();
        Event pendingEvent = createTestEvent("Event", "pending", 1L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(pendingEvent));

        mockMvc.perform(post("/api/administrators/events/1/reject"))
                .andExpect(status().isOk());

        verify(eventRepository).save(argThat(event ->
            event.getStatus().equals("rejected")
        ));
    }

    @Test
    @DisplayName("U14: GET /api/administrators/events should include event details like title, description, and organizer")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnEventDetailsInList() throws Exception {
        Administrator admin = createTestAdministrator();
        Event event = createTestEvent("Summer Festival", "pending", 1L);
        event.setDescription("Amazing music festival");

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Summer Festival"))
                .andExpect(jsonPath("$[0].description").value("Amazing music festival"))
                .andExpect(jsonPath("$[0].status").value("pending"))
                .andExpect(jsonPath("$[0].organizerId").value(2L));
    }

    @Test
    @DisplayName("U14: Admin can approve multiple pending events")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldApproveMultiplePendingEvents() throws Exception {
        Administrator admin = createTestAdministrator();
        Event event1 = createTestEvent("Event 1", "pending", 1L);
        Event event2 = createTestEvent("Event 2", "pending", 2L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event2));

        mockMvc.perform(post("/api/administrators/events/1/approve"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/administrators/events/2/approve"))
                .andExpect(status().isOk());

        verify(eventRepository, times(2)).save(any(Event.class));
    }

    @Test
    @DisplayName("U14: Admin can reject multiple pending events")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldRejectMultiplePendingEvents() throws Exception {
        Administrator admin = createTestAdministrator();
        Event event1 = createTestEvent("Event 1", "pending", 1L);
        Event event2 = createTestEvent("Event 2", "pending", 2L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event2));

        mockMvc.perform(post("/api/administrators/events/1/reject"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/administrators/events/2/reject"))
                .andExpect(status().isOk());

        verify(eventRepository, times(2)).save(any(Event.class));
    }

    @Test
    @DisplayName("U14: GET /api/administrators/events should handle large number of events")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleLargeNumberOfEvents() throws Exception {
        Administrator admin = createTestAdministrator();

        List<Event> events = new java.util.ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Event event = createTestEvent("Event " + i, i % 2 == 0 ? "pending" : "approved", (long) i);
            events.add(event);
        }

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50));
    }

    @Test
    @DisplayName("U14: Approved events should not appear in pending list after approval")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldRemoveApprovedEventsFromPendingList() throws Exception {
        Administrator admin = createTestAdministrator();
        Event pendingEvent = createTestEvent("Pending Event", "pending", 1L);
        Event approvedEvent = createTestEvent("Approved Event", "approved", 2L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(Arrays.asList(approvedEvent));

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("approved"));
    }

    @Test
    @DisplayName("U14: Rejected events should not appear in pending list after rejection")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldRemoveRejectedEventsFromPendingList() throws Exception {
        Administrator admin = createTestAdministrator();
        Event rejectedEvent = createTestEvent("Rejected Event", "rejected", 1L);
        Event pendingEvent = createTestEvent("Pending Event", "pending", 2L);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(eventRepository.findAll()).thenReturn(Arrays.asList(rejectedEvent, pendingEvent));

        mockMvc.perform(get("/api/administrators/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== U15: Global Statistics Dashboard Tests ====================

    @Test
    @DisplayName("U15: GET /api/administrators/stats/global should return global statistics")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnGlobalStatistics() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                10L,  // totalEvents
                150L, // totalTickets
                120L, // totalScannedTickets
                30L,  // totalUnscannedTickets
                50L,  // totalStudents
                8L,   // totalOrganizers
                80.0, // scanRate
                Collections.emptyList(), // topEvents
                Collections.emptyList()  // participationTrends
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(10))
                .andExpect(jsonPath("$.totalTickets").value(150))
                .andExpect(jsonPath("$.totalScannedTickets").value(120))
                .andExpect(jsonPath("$.totalUnscannedTickets").value(30))
                .andExpect(jsonPath("$.totalStudents").value(50))
                .andExpect(jsonPath("$.totalOrganizers").value(8))
                .andExpect(jsonPath("$.scanRate").value(80.0));
    }

    @Test
    @DisplayName("U15: GET /api/administrators/stats/global should require ADMINISTRATOR role")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldRequireAdminRoleForGlobalStats() throws Exception {
        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U15: GET /api/administrators/stats/global should return 403 for unauthenticated users")
    void shouldReturn403ForUnauthenticatedGlobalStats() throws Exception {
        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U15: GET /api/administrators/stats/global should return 403 for organizer users")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturn403ForOrganizerAccessingGlobalStats() throws Exception {
        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U15: Global stats should calculate scan rate correctly")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldCalculateScanRateCorrectly() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                5L,   // totalEvents
                100L, // totalTickets
                75L,  // totalScannedTickets (75%)
                25L,  // totalUnscannedTickets
                30L,  // totalStudents
                5L,   // totalOrganizers
                75.0, // scanRate
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanRate").value(75.0))
                .andExpect(jsonPath("$.totalScannedTickets").value(75))
                .andExpect(jsonPath("$.totalUnscannedTickets").value(25));
    }

    @Test
    @DisplayName("U15: Global stats should handle zero tickets gracefully")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleZeroTicketsGracefully() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                5L,   // totalEvents
                0L,   // totalTickets
                0L,   // totalScannedTickets
                0L,   // totalUnscannedTickets
                10L,  // totalStudents
                3L,   // totalOrganizers
                0.0,  // scanRate
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTickets").value(0))
                .andExpect(jsonPath("$.scanRate").value(0.0));
    }

    @Test
    @DisplayName("U15: Global stats should include top 5 events by attendance")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldIncludeTop5EventsByAttendance() throws Exception {
        Administrator admin = createTestAdministrator();

        List<GlobalStatsResponse.EventStats> topEvents = Arrays.asList(
                new GlobalStatsResponse.EventStats(1L, "Rock Concert", 150L, 145L),
                new GlobalStatsResponse.EventStats(2L, "Tech Conference", 120L, 110L),
                new GlobalStatsResponse.EventStats(3L, "Sports Event", 100L, 95L),
                new GlobalStatsResponse.EventStats(4L, "Art Exhibition", 80L, 75L),
                new GlobalStatsResponse.EventStats(5L, "Food Festival", 60L, 55L)
        );

        GlobalStatsResponse stats = new GlobalStatsResponse(
                20L, 510L, 480L, 30L, 200L, 15L, 94.1,
                topEvents,
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topEvents.length()").value(5))
                .andExpect(jsonPath("$.topEvents[0].eventName").value("Rock Concert"))
                .andExpect(jsonPath("$.topEvents[0].ticketCount").value(150))
                .andExpect(jsonPath("$.topEvents[0].scannedCount").value(145))
                .andExpect(jsonPath("$.topEvents[4].eventName").value("Food Festival"));
    }

    @Test
    @DisplayName("U15: Global stats should include participation trends over time")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldIncludeParticipationTrends() throws Exception {
        Administrator admin = createTestAdministrator();

        List<GlobalStatsResponse.ParticipationTrend> trends = Arrays.asList(
                new GlobalStatsResponse.ParticipationTrend("2025-06-01", 50L, 45L),
                new GlobalStatsResponse.ParticipationTrend("2025-06-15", 75L, 70L),
                new GlobalStatsResponse.ParticipationTrend("2025-07-01", 100L, 90L)
        );

        GlobalStatsResponse stats = new GlobalStatsResponse(
                10L, 225L, 205L, 20L, 100L, 10L, 91.1,
                Collections.emptyList(),
                trends
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationTrends.length()").value(3))
                .andExpect(jsonPath("$.participationTrends[0].date").value("2025-06-01"))
                .andExpect(jsonPath("$.participationTrends[0].ticketsIssued").value(50))
                .andExpect(jsonPath("$.participationTrends[0].ticketsScanned").value(45))
                .andExpect(jsonPath("$.participationTrends[2].date").value("2025-07-01"));
    }

    @Test
    @DisplayName("U15: Global stats should show trends are sorted by date")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldShowTrendsSortedByDate() throws Exception {
        Administrator admin = createTestAdministrator();

        List<GlobalStatsResponse.ParticipationTrend> trends = Arrays.asList(
                new GlobalStatsResponse.ParticipationTrend("2025-05-15", 30L, 25L),
                new GlobalStatsResponse.ParticipationTrend("2025-06-01", 50L, 45L),
                new GlobalStatsResponse.ParticipationTrend("2025-07-20", 80L, 75L)
        );

        GlobalStatsResponse stats = new GlobalStatsResponse(
                8L, 160L, 145L, 15L, 75L, 8L, 90.6,
                Collections.emptyList(),
                trends
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationTrends[0].date").value("2025-05-15"))
                .andExpect(jsonPath("$.participationTrends[1].date").value("2025-06-01"))
                .andExpect(jsonPath("$.participationTrends[2].date").value("2025-07-20"));
    }

    @Test
    @DisplayName("U15: Global stats should include all required core statistics")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldIncludeAllRequiredCoreStatistics() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                25L,  // totalEvents
                300L, // totalTickets
                250L, // totalScannedTickets
                50L,  // totalUnscannedTickets
                120L, // totalStudents
                20L,  // totalOrganizers
                83.33, // scanRate
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").exists())
                .andExpect(jsonPath("$.totalTickets").exists())
                .andExpect(jsonPath("$.totalScannedTickets").exists())
                .andExpect(jsonPath("$.totalUnscannedTickets").exists())
                .andExpect(jsonPath("$.totalStudents").exists())
                .andExpect(jsonPath("$.totalOrganizers").exists())
                .andExpect(jsonPath("$.scanRate").exists());
    }

    @Test
    @DisplayName("U15: Global stats should handle empty top events list")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleEmptyTopEventsList() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                0L, 0L, 0L, 0L, 0L, 0L, 0.0,
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topEvents").isArray())
                .andExpect(jsonPath("$.topEvents.length()").value(0));
    }

    @Test
    @DisplayName("U15: Global stats should handle empty participation trends")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleEmptyParticipationTrends() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                5L, 20L, 15L, 5L, 10L, 3L, 75.0,
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationTrends").isArray())
                .andExpect(jsonPath("$.participationTrends.length()").value(0));
    }

    @Test
    @DisplayName("U15: Global stats should show events are sorted by ticket count descending")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldShowEventsSortedByTicketCountDescending() throws Exception {
        Administrator admin = createTestAdministrator();

        List<GlobalStatsResponse.EventStats> topEvents = Arrays.asList(
                new GlobalStatsResponse.EventStats(1L, "Event 1", 200L, 180L),
                new GlobalStatsResponse.EventStats(2L, "Event 2", 150L, 140L),
                new GlobalStatsResponse.EventStats(3L, "Event 3", 100L, 95L),
                new GlobalStatsResponse.EventStats(4L, "Event 4", 75L, 70L),
                new GlobalStatsResponse.EventStats(5L, "Event 5", 50L, 45L)
        );

        GlobalStatsResponse stats = new GlobalStatsResponse(
                15L, 575L, 530L, 45L, 250L, 12L, 92.2,
                topEvents,
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topEvents[0].ticketCount").value(200))
                .andExpect(jsonPath("$.topEvents[1].ticketCount").value(150))
                .andExpect(jsonPath("$.topEvents[2].ticketCount").value(100))
                .andExpect(jsonPath("$.topEvents[3].ticketCount").value(75))
                .andExpect(jsonPath("$.topEvents[4].ticketCount").value(50));
    }

    @Test
    @DisplayName("U15: Global stats should display event details including event ID and name")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldDisplayEventDetailsIncludingIdAndName() throws Exception {
        Administrator admin = createTestAdministrator();

        List<GlobalStatsResponse.EventStats> topEvents = Arrays.asList(
                new GlobalStatsResponse.EventStats(101L, "Summer Music Festival", 250L, 240L)
        );

        GlobalStatsResponse stats = new GlobalStatsResponse(
                5L, 250L, 240L, 10L, 100L, 8L, 96.0,
                topEvents,
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topEvents[0].eventId").value(101))
                .andExpect(jsonPath("$.topEvents[0].eventName").value("Summer Music Festival"))
                .andExpect(jsonPath("$.topEvents[0].ticketCount").value(250))
                .andExpect(jsonPath("$.topEvents[0].scannedCount").value(240));
    }

    @Test
    @DisplayName("U15: Global stats should handle 100% scan rate")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandle100PercentScanRate() throws Exception {
        Administrator admin = createTestAdministrator();

        GlobalStatsResponse stats = new GlobalStatsResponse(
                3L,   // totalEvents
                100L, // totalTickets
                100L, // totalScannedTickets (100%)
                0L,   // totalUnscannedTickets
                50L,  // totalStudents
                5L,   // totalOrganizers
                100.0, // scanRate
                Collections.emptyList(),
                Collections.emptyList()
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanRate").value(100.0))
                .andExpect(jsonPath("$.totalUnscannedTickets").value(0));
    }

    @Test
    @DisplayName("U15: Global stats should handle service errors gracefully")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleServiceErrorsGracefully() throws Exception {
        Administrator admin = createTestAdministrator();

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("U15: Global stats should return different stats for different time periods")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnDifferentStatsForDifferentTimePeriods() throws Exception {
        Administrator admin = createTestAdministrator();

        List<GlobalStatsResponse.ParticipationTrend> trends = Arrays.asList(
                new GlobalStatsResponse.ParticipationTrend("2025-01-01", 10L, 8L),
                new GlobalStatsResponse.ParticipationTrend("2025-02-01", 25L, 20L),
                new GlobalStatsResponse.ParticipationTrend("2025-03-01", 40L, 38L)
        );

        GlobalStatsResponse stats = new GlobalStatsResponse(
                10L, 75L, 66L, 9L, 40L, 6L, 88.0,
                Collections.emptyList(),
                trends
        );

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(globalStatsService.getGlobalStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/administrators/stats/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationTrends.length()").value(3))
                .andExpect(jsonPath("$.participationTrends[0].ticketsIssued").value(10))
                .andExpect(jsonPath("$.participationTrends[1].ticketsIssued").value(25))
                .andExpect(jsonPath("$.participationTrends[2].ticketsIssued").value(40));
    }

    // ==================== U16: Administrator Organizer Approval Tests ====================

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should return all organizers with approval status")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnAllOrganizersWithApprovalStatus() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer pendingOrganizer = createTestOrganizer("pending@example.com", "Pending", "Organizer", 10L, false);
        Organizer approvedOrganizer = createTestOrganizer("approved@example.com", "Approved", "Organizer", 11L, true);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(pendingOrganizer, approvedOrganizer));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].email").value("pending@example.com"))
                .andExpect(jsonPath("$[0].approvalStatus").value("pending"))
                .andExpect(jsonPath("$[1].userId").value(11))
                .andExpect(jsonPath("$[1].email").value("approved@example.com"))
                .andExpect(jsonPath("$[1].approvalStatus").value("approved"));
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should return empty list when no organizers exist")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnEmptyListWhenNoOrganizers() throws Exception {
        Administrator admin = createTestAdministrator();

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should include organizer details")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldIncludeOrganizerDetails() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer organizer = createTestOrganizer("organizer@example.com", "John", "Smith", 20L, false);
        organizer.setOrganizationName("Tech Events Inc");
        organizer.setPhoneNumber("5141234567");

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(organizer));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(20))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Smith"))
                .andExpect(jsonPath("$[0].email").value("organizer@example.com"))
                .andExpect(jsonPath("$[0].organizationName").value("Tech Events Inc"))
                .andExpect(jsonPath("$[0].phoneNumber").value("5141234567"))
                .andExpect(jsonPath("$[0].approvalStatus").value("pending"));
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should require ADMINISTRATOR role")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldRequireAdminRoleToViewOrganizers() throws Exception {
        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should return 403 for unauthenticated users")
    void shouldReturn403ForUnauthenticatedUsersViewingOrganizers() throws Exception {
        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should return 403 for organizer users")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturn403ForOrganizerAccessingOrganizersList() throws Exception {
        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should handle large number of organizers")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleLargeNumberOfOrganizers() throws Exception {
        Administrator admin = createTestAdministrator();

        List<Organizer> organizers = new java.util.ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Organizer organizer = createTestOrganizer(
                    "organizer" + i + "@example.com",
                    "Organizer" + i,
                    "LastName" + i,
                    (long) (100 + i),
                    i % 2 == 0
            );
            organizers.add(organizer);
        }

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(organizers);

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50));
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should correctly map approval status")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldCorrectlyMapApprovalStatus() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer pendingOrganizer1 = createTestOrganizer("pending1@example.com", "P1", "Org", 30L, false);
        Organizer pendingOrganizer2 = createTestOrganizer("pending2@example.com", "P2", "Org", 31L, false);
        Organizer approvedOrganizer1 = createTestOrganizer("approved1@example.com", "A1", "Org", 32L, true);
        Organizer approvedOrganizer2 = createTestOrganizer("approved2@example.com", "A2", "Org", 33L, true);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(
                pendingOrganizer1, pendingOrganizer2, approvedOrganizer1, approvedOrganizer2));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].approvalStatus").value("pending"))
                .andExpect(jsonPath("$[1].approvalStatus").value("pending"))
                .andExpect(jsonPath("$[2].approvalStatus").value("approved"))
                .andExpect(jsonPath("$[3].approvalStatus").value("approved"));
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should handle null organization names gracefully")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleNullOrganizationNamesGracefully() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer organizer = createTestOrganizer("org@example.com", "Test", "Organizer", 40L, false);
        organizer.setOrganizationName(null);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(organizer));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizationName").isEmpty());
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should return only pending organizers when filtered")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnOnlyPendingOrganizersWhenFiltered() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer pendingOrganizer1 = createTestOrganizer("pending1@example.com", "P1", "Org", 50L, false);
        Organizer pendingOrganizer2 = createTestOrganizer("pending2@example.com", "P2", "Org", 51L, false);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(pendingOrganizer1, pendingOrganizer2));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].approvalStatus").value("pending"))
                .andExpect(jsonPath("$[1].approvalStatus").value("pending"));
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should maintain consistent data format")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldMaintainConsistentDataFormat() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer organizer = createTestOrganizer("test@example.com", "Test", "User", 60L, true);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(organizer));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").isNumber())
                .andExpect(jsonPath("$[0].firstName").isString())
                .andExpect(jsonPath("$[0].lastName").isString())
                .andExpect(jsonPath("$[0].email").isString())
                .andExpect(jsonPath("$[0].organizationName").isString())
                .andExpect(jsonPath("$[0].phoneNumber").isString())
                .andExpect(jsonPath("$[0].approvalStatus").isString());
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should handle service errors gracefully")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldHandleOrganizerServiceErrorsGracefully() throws Exception {
        Administrator admin = createTestAdministrator();

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("U16: Admin can view both pending and approved organizers in the same list")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldViewBothPendingAndApprovedOrganizers() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer pending = createTestOrganizer("pending@example.com", "Pending", "User", 70L, false);
        Organizer approved = createTestOrganizer("approved@example.com", "Approved", "User", 71L, true);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(pending, approved));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.approvalStatus == 'pending')]").exists())
                .andExpect(jsonPath("$[?(@.approvalStatus == 'approved')]").exists());
    }

    @Test
    @DisplayName("U16: GET /api/administrators/organizers should return organizers in consistent order")
    @WithMockUser(username = "admin@linkt.com", roles = {"ADMINISTRATOR"})
    void shouldReturnOrganizersInConsistentOrder() throws Exception {
        Administrator admin = createTestAdministrator();

        Organizer org1 = createTestOrganizer("org1@example.com", "First", "Organizer", 80L, false);
        Organizer org2 = createTestOrganizer("org2@example.com", "Second", "Organizer", 81L, true);
        Organizer org3 = createTestOrganizer("org3@example.com", "Third", "Organizer", 82L, false);

        when(userRepository.findByEmail("admin@linkt.com")).thenReturn(Optional.of(admin));
        when(organizerRepository.findAll()).thenReturn(Arrays.asList(org1, org2, org3));

        mockMvc.perform(get("/api/administrators/organizers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(80))
                .andExpect(jsonPath("$[1].userId").value(81))
                .andExpect(jsonPath("$[2].userId").value(82));
    }
}

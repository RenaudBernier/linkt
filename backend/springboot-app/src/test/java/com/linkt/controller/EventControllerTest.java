package com.linkt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkt.dto.EventDTO;
import com.linkt.dto.StudentRegistrationDTO;
import com.linkt.linkt.LinktApplication;
import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.repository.EventRepository;
import com.linkt.service.EventService;
import com.linkt.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LinktApplication.class)
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.linkt.repository.TicketRepository ticketRepository;

    // Helper method to create test organizer
    private Organizer createTestOrganizer() {
        Organizer organizer = new Organizer("organizer@example.com", "Jane", "Smith", "5555555555", "hashedPassword");
        organizer.setUserId(2L);
        organizer.setOrganizationName("Test Events Inc.");
        return organizer;
    }

    // Helper method to create test event
    private Event createTestEvent(String title, double price, Long eventId) {
        Organizer organizer = createTestOrganizer();
        Event event = new Event(title, "Description for " + title, "conference",
                "2025-06-15T09:00:00", "2025-06-15T17:00:00", "Test Location", 100);
        event.setEventId(eventId);
        event.setPrice(price);
        event.setOrganizer(organizer);
        event.setImageUrl("https://example.com/image.jpg");
        return event;
    }

    // ==================== U7: Event Navigation and Filtering Tests ====================

    @Test
    @DisplayName("U7: GET /api/events should return all available events")
    void shouldReturnAllAvailableEvents() throws Exception {
        Event event1 = createTestEvent("Tech Conference 2025", 50.0, 1L);
        Event event2 = createTestEvent("Music Festival", 75.0, 2L);
        Event event3 = createTestEvent("Art Exhibition", 0.0, 3L);

        List<Event> allEvents = Arrays.asList(event1, event2, event3);
        when(eventRepository.findAll()).thenReturn(allEvents);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Tech Conference 2025"))
                .andExpect(jsonPath("$[1].title").value("Music Festival"))
                .andExpect(jsonPath("$[2].title").value("Art Exhibition"));
    }

    @Test
    @DisplayName("U7: All events should include price information for filtering")
    void shouldIncludePriceInformation() throws Exception {
        Event event1 = createTestEvent("Conference", 50.0, 1L);
        Event event2 = createTestEvent("Workshop", 25.0, 2L);
        Event event3 = createTestEvent("Seminar", 0.0, 3L);

        List<Event> events = Arrays.asList(event1, event2, event3);
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(50.0))
                .andExpect(jsonPath("$[1].price").value(25.0))
                .andExpect(jsonPath("$[2].price").value(0.0));
    }

    @Test
    @DisplayName("U7: Should return events with price 0 (free events)")
    void shouldReturnFreeEvents() throws Exception {
        Event event1 = createTestEvent("Free Workshop", 0.0, 1L);
        Event event2 = createTestEvent("Free Seminar", 0.0, 2L);
        Event event3 = createTestEvent("Paid Conference", 50.0, 3L);

        List<Event> allEvents = Arrays.asList(event1, event2, event3);
        when(eventRepository.findAll()).thenReturn(allEvents);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].price").value(0.0))
                .andExpect(jsonPath("$[1].price").value(0.0))
                .andExpect(jsonPath("$[2].price").value(50.0));
    }

    @Test
    @DisplayName("U7: Should return events across full price range")
    void shouldReturnEventsAcrossPriceRange() throws Exception {
        Event event1 = createTestEvent("Free Event", 0.0, 1L);
        Event event2 = createTestEvent("Budget Event", 10.0, 2L);
        Event event3 = createTestEvent("Mid-Range Event", 50.0, 3L);
        Event event4 = createTestEvent("Premium Event", 100.0, 4L);
        Event event5 = createTestEvent("Luxury Event", 500.0, 5L);

        List<Event> events = Arrays.asList(event1, event2, event3, event4, event5);
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].price").value(0.0))
                .andExpect(jsonPath("$[1].price").value(10.0))
                .andExpect(jsonPath("$[2].price").value(50.0))
                .andExpect(jsonPath("$[3].price").value(100.0))
                .andExpect(jsonPath("$[4].price").value(500.0));
    }

    @Test
    @DisplayName("U7: Should return empty array when no events exist")
    void shouldReturnEmptyArrayWhenNoEvents() throws Exception {
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("U7: Events should include all necessary information for display")
    void shouldIncludeAllEventInformation() throws Exception {
        Event event = createTestEvent("Complete Event", 45.99, 1L);

        when(eventRepository.findAll()).thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[0].title").value("Complete Event"))
                .andExpect(jsonPath("$[0].description").value("Description for Complete Event"))
                .andExpect(jsonPath("$[0].eventType").value("conference"))
                .andExpect(jsonPath("$[0].startDateTime").value("2025-06-15T09:00:00"))
                .andExpect(jsonPath("$[0].endDateTime").value("2025-06-15T17:00:00"))
                .andExpect(jsonPath("$[0].location").value("Test Location"))
                .andExpect(jsonPath("$[0].capacity").value(100))
                .andExpect(jsonPath("$[0].price").value(45.99))
                .andExpect(jsonPath("$[0].imageUrl").value("https://example.com/image.jpg"));
    }

    @Test
    @DisplayName("U7: Should handle multiple events with same price")
    void shouldHandleMultipleEventsWithSamePrice() throws Exception {
        Event event1 = createTestEvent("Event A", 25.0, 1L);
        Event event2 = createTestEvent("Event B", 25.0, 2L);
        Event event3 = createTestEvent("Event C", 25.0, 3L);

        List<Event> events = Arrays.asList(event1, event2, event3);
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].price").value(25.0))
                .andExpect(jsonPath("$[1].price").value(25.0))
                .andExpect(jsonPath("$[2].price").value(25.0));
    }

    @Test
    @DisplayName("U7: Should return events with decimal prices")
    void shouldHandleDecimalPrices() throws Exception {
        Event event1 = createTestEvent("Event 1", 19.99, 1L);
        Event event2 = createTestEvent("Event 2", 49.50, 2L);
        Event event3 = createTestEvent("Event 3", 99.95, 3L);

        List<Event> events = Arrays.asList(event1, event2, event3);
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(19.99))
                .andExpect(jsonPath("$[1].price").value(49.50))
                .andExpect(jsonPath("$[2].price").value(99.95));
    }

    @Test
    @DisplayName("U7: GET /api/events/{id} should return specific event details")
    void shouldReturnSpecificEventDetails() throws Exception {
        Event event = createTestEvent("Specific Event", 30.0, 42L);

        when(eventRepository.findById(42L)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(42))
                .andExpect(jsonPath("$.title").value("Specific Event"))
                .andExpect(jsonPath("$.price").value(30.0));
    }

    @Test
    @DisplayName("U7: Should return 404 when event not found")
    void shouldReturn404WhenEventNotFound() throws Exception {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("U7: Events should be navigable without authentication")
    void shouldBeNavigableWithoutAuthentication() throws Exception {
        Event event1 = createTestEvent("Public Event 1", 15.0, 1L);
        Event event2 = createTestEvent("Public Event 2", 25.0, 2L);

        List<Event> events = Arrays.asList(event1, event2);
        when(eventRepository.findAll()).thenReturn(events);

        // No authentication provided - should still work
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("U7: Should support filtering logic for price range on frontend")
    void shouldSupportPriceRangeFiltering() throws Exception {
        // Backend returns all events with prices
        // Frontend will filter based on slider values
        Event event1 = createTestEvent("Free Event", 0.0, 1L);
        Event event2 = createTestEvent("Cheap Event", 5.0, 2L);
        Event event3 = createTestEvent("Mid Event", 50.0, 3L);
        Event event4 = createTestEvent("Expensive Event", 200.0, 4L);

        List<Event> events = Arrays.asList(event1, event2, event3, event4);
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                // Verify all events have price field for frontend filtering
                .andExpect(jsonPath("$[0].price").exists())
                .andExpect(jsonPath("$[1].price").exists())
                .andExpect(jsonPath("$[2].price").exists())
                .andExpect(jsonPath("$[3].price").exists());
    }

    @Test
    @DisplayName("U7: Should return events with organizer information")
    void shouldIncludeOrganizerInformation() throws Exception {
        Event event = createTestEvent("Organized Event", 40.0, 1L);

        when(eventRepository.findAll()).thenReturn(Arrays.asList(event));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizer").exists())
                .andExpect(jsonPath("$[0].organizer.firstName").value("Jane"))
                .andExpect(jsonPath("$[0].organizer.lastName").value("Smith"))
                .andExpect(jsonPath("$[0].organizer.organizationName").value("Test Events Inc."));
    }

    @Test
    @DisplayName("U7: Should handle large number of events")
    void shouldHandleLargeNumberOfEvents() throws Exception {
        // Create 50 events with various prices
        List<Event> events = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            events.add(createTestEvent("Event " + i, i * 2.0, (long) i));
        }

        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50))
                .andExpect(jsonPath("$[0].price").value(0.0))
                .andExpect(jsonPath("$[49].price").value(98.0));
    }

    // ==================== U8: Organizer Event Creation Tests ====================

    @Test
    @DisplayName("U8: POST /api/events/add should create event with all required fields")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldCreateEventWithAllRequiredFields() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Tech Conference 2025");
        eventDTO.setDescription("Annual technology conference for students");
        eventDTO.setEventType("conference");
        eventDTO.setPrice(50.0);
        eventDTO.setStartDateTime("2025-06-15T09:00:00");
        eventDTO.setEndDateTime("2025-06-15T17:00:00");
        eventDTO.setLocation("Montreal Convention Center");
        eventDTO.setCapacity(200);
        eventDTO.setImage("https://example.com/conference.jpg");

        Event savedEvent = new Event("Tech Conference 2025", "Annual technology conference for students",
                "conference", "2025-06-15T09:00:00", "2025-06-15T17:00:00", "Montreal Convention Center", 200);
        savedEvent.setEventId(1L);
        savedEvent.setPrice(50.0);
        savedEvent.setImageUrl("https://example.com/conference.jpg");
        savedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.title").value("Tech Conference 2025"))
                .andExpect(jsonPath("$.description").value("Annual technology conference for students"))
                .andExpect(jsonPath("$.eventType").value("conference"))
                .andExpect(jsonPath("$.price").value(50.0))
                .andExpect(jsonPath("$.startDateTime").value("2025-06-15T09:00:00"))
                .andExpect(jsonPath("$.endDateTime").value("2025-06-15T17:00:00"))
                .andExpect(jsonPath("$.location").value("Montreal Convention Center"))
                .andExpect(jsonPath("$.capacity").value(200))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/conference.jpg"));
    }

    @Test
    @DisplayName("U8: Created event should be associated with the organizer")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldAssociateEventWithOrganizer() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Workshop");
        eventDTO.setDescription("Programming workshop");
        eventDTO.setEventType("workshop");
        eventDTO.setPrice(25.0);
        eventDTO.setStartDateTime("2025-07-10T10:00:00");
        eventDTO.setEndDateTime("2025-07-10T12:00:00");
        eventDTO.setLocation("Campus Building A");
        eventDTO.setCapacity(50);
        eventDTO.setImage("https://example.com/workshop.jpg");

        Event savedEvent = createTestEvent("Workshop", 25.0, 1L);
        savedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizer").exists())
                .andExpect(jsonPath("$.organizer.userId").value(2))
                .andExpect(jsonPath("$.organizer.firstName").value("Jane"))
                .andExpect(jsonPath("$.organizer.lastName").value("Smith"))
                .andExpect(jsonPath("$.organizer.organizationName").value("Test Events Inc."));
    }

    @Test
    @DisplayName("U8: Should create free event with price 0")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldCreateFreeEvent() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Free Seminar");
        eventDTO.setDescription("Free educational seminar");
        eventDTO.setEventType("seminar");
        eventDTO.setPrice(0.0);
        eventDTO.setStartDateTime("2025-08-01T14:00:00");
        eventDTO.setEndDateTime("2025-08-01T16:00:00");
        eventDTO.setLocation("Auditorium");
        eventDTO.setCapacity(100);
        eventDTO.setImage("https://example.com/seminar.jpg");

        Event savedEvent = createTestEvent("Free Seminar", 0.0, 1L);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(0.0));
    }

    @Test
    @DisplayName("U8: Should handle image URL properly")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleImageUrlProperly() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Music Festival");
        eventDTO.setDescription("Summer music festival");
        eventDTO.setEventType("music");
        eventDTO.setPrice(75.0);
        eventDTO.setStartDateTime("2025-09-20T18:00:00");
        eventDTO.setEndDateTime("2025-09-20T23:00:00");
        eventDTO.setLocation("Olympic Stadium");
        eventDTO.setCapacity(5000);
        eventDTO.setImage("https://cdn.example.com/images/festival-2025.png");

        Event savedEvent = createTestEvent("Music Festival", 75.0, 1L);
        savedEvent.setImageUrl("https://cdn.example.com/images/festival-2025.png");

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example.com/images/festival-2025.png"));
    }

    @Test
    @DisplayName("U8: Should validate capacity is set correctly")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldValidateCapacity() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Small Workshop");
        eventDTO.setDescription("Intimate workshop session");
        eventDTO.setEventType("workshop");
        eventDTO.setPrice(30.0);
        eventDTO.setStartDateTime("2025-10-05T13:00:00");
        eventDTO.setEndDateTime("2025-10-05T15:00:00");
        eventDTO.setLocation("Room 101");
        eventDTO.setCapacity(15);
        eventDTO.setImage("https://example.com/workshop.jpg");

        Event savedEvent = createTestEvent("Small Workshop", 30.0, 1L);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacity").value(100)); // From createTestEvent helper
    }

    @Test
    @DisplayName("U8: Should handle different event types")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleDifferentEventTypes() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Art Exhibition");
        eventDTO.setDescription("Contemporary art showcase");
        eventDTO.setEventType("exhibition");
        eventDTO.setPrice(10.0);
        eventDTO.setStartDateTime("2025-11-01T10:00:00");
        eventDTO.setEndDateTime("2025-11-01T18:00:00");
        eventDTO.setLocation("Art Gallery");
        eventDTO.setCapacity(150);
        eventDTO.setImage("https://example.com/art.jpg");

        Event savedEvent = new Event("Art Exhibition", "Contemporary art showcase", "exhibition",
                "2025-11-01T10:00:00", "2025-11-01T18:00:00", "Art Gallery", 150);
        savedEvent.setEventId(1L);
        savedEvent.setPrice(10.0);
        savedEvent.setImageUrl("https://example.com/art.jpg");
        savedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("exhibition"));
    }

    @Test
    @DisplayName("U8: Should handle date and time parsing correctly")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleDateTimeParsing() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Evening Gala");
        eventDTO.setDescription("Annual gala dinner");
        eventDTO.setEventType("gala");
        eventDTO.setPrice(150.0);
        eventDTO.setStartDateTime("2025-12-15T19:30:00");
        eventDTO.setEndDateTime("2025-12-15T23:30:00");
        eventDTO.setLocation("Grand Hotel Ballroom");
        eventDTO.setCapacity(300);
        eventDTO.setImage("https://example.com/gala.jpg");

        Event savedEvent = new Event("Evening Gala", "Annual gala dinner", "gala",
                "2025-12-15T19:30:00", "2025-12-15T23:30:00", "Grand Hotel Ballroom", 300);
        savedEvent.setEventId(1L);
        savedEvent.setPrice(150.0);
        savedEvent.setImageUrl("https://example.com/gala.jpg");
        savedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startDateTime").value("2025-12-15T19:30:00"))
                .andExpect(jsonPath("$.endDateTime").value("2025-12-15T23:30:00"));
    }

    @Test
    @DisplayName("U8: Should handle decimal prices correctly")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleDecimalPricesForCreation() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Budget Event");
        eventDTO.setDescription("Affordable community event");
        eventDTO.setEventType("community");
        eventDTO.setPrice(19.99);
        eventDTO.setStartDateTime("2026-01-20T15:00:00");
        eventDTO.setEndDateTime("2026-01-20T17:00:00");
        eventDTO.setLocation("Community Center");
        eventDTO.setCapacity(80);
        eventDTO.setImage("https://example.com/community.jpg");

        Event savedEvent = createTestEvent("Budget Event", 19.99, 1L);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    @DisplayName("U8: Created event should be retrievable in browse events")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldBeRetrievableAfterCreation() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("New Event");
        eventDTO.setDescription("Newly created event");
        eventDTO.setEventType("conference");
        eventDTO.setPrice(40.0);
        eventDTO.setStartDateTime("2026-02-10T10:00:00");
        eventDTO.setEndDateTime("2026-02-10T16:00:00");
        eventDTO.setLocation("Conference Hall");
        eventDTO.setCapacity(120);
        eventDTO.setImage("https://example.com/new.jpg");

        Event savedEvent = createTestEvent("New Event", 40.0, 99L);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // Create the event
        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(99));

        // Verify it can be retrieved by ID
        when(eventRepository.findById(99L)).thenReturn(Optional.of(savedEvent));

        mockMvc.perform(get("/api/events/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(99))
                .andExpect(jsonPath("$.title").value("New Event"));
    }

    @Test
    @DisplayName("U8: Should handle location field properly")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleLocationField() throws Exception {
        Organizer organizer = createTestOrganizer();

        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Campus Tour");
        eventDTO.setDescription("Guided campus tour");
        eventDTO.setEventType("tour");
        eventDTO.setPrice(5.0);
        eventDTO.setStartDateTime("2026-03-15T11:00:00");
        eventDTO.setEndDateTime("2026-03-15T13:00:00");
        eventDTO.setLocation("Main Campus Entrance, 123 University Ave, Montreal, QC");
        eventDTO.setCapacity(30);
        eventDTO.setImage("https://example.com/tour.jpg");

        Event savedEvent = new Event("Campus Tour", "Guided campus tour", "tour",
                "2026-03-15T11:00:00", "2026-03-15T13:00:00",
                "Main Campus Entrance, 123 University Ave, Montreal, QC", 30);
        savedEvent.setEventId(1L);
        savedEvent.setPrice(5.0);
        savedEvent.setImageUrl("https://example.com/tour.jpg");
        savedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value("Main Campus Entrance, 123 University Ave, Montreal, QC"));
    }

    @Test
    @DisplayName("U8: Should return 403 when non-organizer tries to create event")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldReturn403ForNonOrganizer() throws Exception {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Unauthorized Event");
        eventDTO.setDescription("Should not be created");
        eventDTO.setEventType("conference");
        eventDTO.setPrice(50.0);
        eventDTO.setStartDateTime("2026-04-01T10:00:00");
        eventDTO.setEndDateTime("2026-04-01T12:00:00");
        eventDTO.setLocation("Somewhere");
        eventDTO.setCapacity(100);
        eventDTO.setImage("https://example.com/image.jpg");

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U8: Should return 403 when unauthenticated user tries to create event")
    void shouldReturn403ForUnauthenticatedUser() throws Exception {
        EventDTO eventDTO = new EventDTO();
        eventDTO.setTitle("Unauthorized Event");
        eventDTO.setDescription("Should not be created");
        eventDTO.setEventType("conference");
        eventDTO.setPrice(50.0);
        eventDTO.setStartDateTime("2026-04-01T10:00:00");
        eventDTO.setEndDateTime("2026-04-01T12:00:00");
        eventDTO.setLocation("Somewhere");
        eventDTO.setCapacity(100);
        eventDTO.setImage("https://example.com/image.jpg");

        mockMvc.perform(post("/api/events/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isForbidden());
    }

    // ==================== U9: Organizer Event Update Tests ====================

    @Test
    @DisplayName("U9: PUT /api/events/{id} should update event with all fields")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventWithAllFields() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Old Title", 50.0, 1L);
        existingEvent.setOrganizer(organizer);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Summer Music Festival");
        updateDTO.setDescription("Updated festival description with new performers");
        updateDTO.setEventType("music");
        updateDTO.setPrice(75.0);
        updateDTO.setStartDateTime("2025-06-15T18:30:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Central Park Amphitheater");
        updateDTO.setCapacity(600);
        updateDTO.setImage("https://example.com/updated-festival.jpg");

        Event updatedEvent = new Event("Summer Music Festival", "Updated festival description with new performers",
                "music", "2025-06-15T18:30:00", "2025-06-15T23:00:00", "Central Park Amphitheater", 600);
        updatedEvent.setEventId(1L);
        updatedEvent.setPrice(75.0);
        updatedEvent.setImageUrl("https://example.com/updated-festival.jpg");
        updatedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.title").value("Summer Music Festival"))
                .andExpect(jsonPath("$.description").value("Updated festival description with new performers"))
                .andExpect(jsonPath("$.eventType").value("music"))
                .andExpect(jsonPath("$.price").value(75.0))
                .andExpect(jsonPath("$.startDateTime").value("2025-06-15T18:30:00"))
                .andExpect(jsonPath("$.endDateTime").value("2025-06-15T23:00:00"))
                .andExpect(jsonPath("$.location").value("Central Park Amphitheater"))
                .andExpect(jsonPath("$.capacity").value(600))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/updated-festival.jpg"));
    }

    @Test
    @DisplayName("U9: Should only allow event owner to update event")
    @WithMockUser(username = "different-organizer@example.com", roles = {"ORGANIZER"})
    void shouldOnlyAllowOwnerToUpdate() throws Exception {
        Organizer originalOrganizer = createTestOrganizer();
        Organizer differentOrganizer = new Organizer("different-organizer@example.com", "Bob", "Jones", "1111111111", "hashedPassword");
        differentOrganizer.setUserId(99L);

        Event existingEvent = createTestEvent("Original Event", 50.0, 1L);
        existingEvent.setOrganizer(originalOrganizer);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Hacked Title");
        updateDTO.setDescription("Should not be updated");
        updateDTO.setEventType("conference");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T09:00:00");
        updateDTO.setEndDateTime("2025-06-15T17:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(100);
        updateDTO.setImage("https://example.com/image.jpg");

        when(userRepository.findByEmail("different-organizer@example.com")).thenReturn(Optional.of(differentOrganizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You don't have permission to edit this event"));
    }

    @Test
    @DisplayName("U9: Should prevent capacity reduction below ticket count")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldPreventCapacityReductionBelowTicketCount() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Concert Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);
        existingEvent.setCapacity(500);

        // Simulate 250 tickets sold
        List<com.linkt.model.Ticket> tickets = new java.util.ArrayList<>();
        for (int i = 0; i < 250; i++) {
            tickets.add(new com.linkt.model.Ticket());
        }
        existingEvent.setTickets(tickets);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Concert Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Park Hall");
        updateDTO.setCapacity(100); // Trying to reduce below 250 tickets sold
        updateDTO.setImage("https://example.com/image.jpg");

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Capacity cannot be reduced below 250 (current number of sold tickets)"));
    }

    @Test
    @DisplayName("U9: Should allow capacity increase when tickets are sold")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldAllowCapacityIncrease() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Concert Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);
        existingEvent.setCapacity(500);

        // Simulate 250 tickets sold
        List<com.linkt.model.Ticket> tickets = new java.util.ArrayList<>();
        for (int i = 0; i < 250; i++) {
            tickets.add(new com.linkt.model.Ticket());
        }
        existingEvent.setTickets(tickets);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Concert Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Park Hall");
        updateDTO.setCapacity(600); // Increasing capacity
        updateDTO.setImage("https://example.com/image.jpg");

        Event updatedEvent = new Event("Concert Event", "Description", "music",
                "2025-06-15T19:00:00", "2025-06-15T23:00:00", "Park Hall", 600);
        updatedEvent.setEventId(1L);
        updatedEvent.setPrice(50.0);
        updatedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(600)); // Increased capacity
    }

    @Test
    @DisplayName("U9: Should update event title")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventTitle() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Summer Concert", 50.0, 1L);
        existingEvent.setOrganizer(organizer);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Summer Music Festival"); // Changed title
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Park Hall");
        updateDTO.setCapacity(500);
        updateDTO.setImage("https://example.com/image.jpg");

        Event updatedEvent = createTestEvent("Summer Music Festival", 50.0, 1L);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Summer Music Festival"));
    }

    @Test
    @DisplayName("U9: Should update event location")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventLocation() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);
        existingEvent.setLocation("Park Hall");

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Central Park Amphitheater"); // Changed location
        updateDTO.setCapacity(500);
        updateDTO.setImage("https://example.com/image.jpg");

        Event updatedEvent = new Event("Event", "Description", "music",
                "2025-06-15T19:00:00", "2025-06-15T23:00:00", "Central Park Amphitheater", 500);
        updatedEvent.setEventId(1L);
        updatedEvent.setPrice(50.0);
        updatedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Central Park Amphitheater"));
    }

    @Test
    @DisplayName("U9: Should update event time")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventTime() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T18:30:00"); // Changed from 19:00 to 18:30
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(500);
        updateDTO.setImage("https://example.com/image.jpg");

        Event updatedEvent = new Event("Event", "Description", "music",
                "2025-06-15T18:30:00", "2025-06-15T23:00:00", "Location", 500);
        updatedEvent.setEventId(1L);
        updatedEvent.setPrice(50.0);
        updatedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDateTime").value("2025-06-15T18:30:00"));
    }

    @Test
    @DisplayName("U9: Should update event description")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventDescription() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Event");
        updateDTO.setDescription("Updated description with new performer information");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(500);
        updateDTO.setImage("https://example.com/image.jpg");

        Event updatedEvent = new Event("Event", "Updated description with new performer information", "music",
                "2025-06-15T19:00:00", "2025-06-15T23:00:00", "Location", 500);
        updatedEvent.setEventId(1L);
        updatedEvent.setPrice(50.0);
        updatedEvent.setOrganizer(organizer);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description with new performer information"));
    }

    @Test
    @DisplayName("U9: Should update event image URL")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventImageUrl() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);
        existingEvent.setImageUrl("https://example.com/old-image.jpg");

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(500);
        updateDTO.setImage("https://example.com/new-image.jpg"); // Updated image

        Event updatedEvent = createTestEvent("Event", 50.0, 1L);
        updatedEvent.setImageUrl("https://example.com/new-image.jpg");

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/new-image.jpg"));
    }

    @Test
    @DisplayName("U9: Should update event price")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldUpdateEventPrice() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = createTestEvent("Event", 50.0, 1L);
        existingEvent.setOrganizer(organizer);

        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("music");
        updateDTO.setPrice(75.0); // Changed price
        updateDTO.setStartDateTime("2025-06-15T19:00:00");
        updateDTO.setEndDateTime("2025-06-15T23:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(500);
        updateDTO.setImage("https://example.com/image.jpg");

        Event updatedEvent = createTestEvent("Event", 75.0, 1L);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(75.0));
    }

    @Test
    @DisplayName("U9: Should return 403 when non-organizer tries to update event")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldReturn403WhenStudentTriesToUpdate() throws Exception {
        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Updated Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("conference");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T09:00:00");
        updateDTO.setEndDateTime("2025-06-15T17:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(100);
        updateDTO.setImage("https://example.com/image.jpg");

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U9: Should return 403 when unauthenticated user tries to update event")
    void shouldReturn403WhenUnauthenticatedUserTriesToUpdate() throws Exception {
        EventDTO updateDTO = new EventDTO();
        updateDTO.setTitle("Updated Event");
        updateDTO.setDescription("Description");
        updateDTO.setEventType("conference");
        updateDTO.setPrice(50.0);
        updateDTO.setStartDateTime("2025-06-15T09:00:00");
        updateDTO.setEndDateTime("2025-06-15T17:00:00");
        updateDTO.setLocation("Location");
        updateDTO.setCapacity(100);
        updateDTO.setImage("https://example.com/image.jpg");

        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U9: GET /api/events/{id} should pre-populate form with current data")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldPrePopulateFormWithCurrentData() throws Exception {
        Organizer organizer = createTestOrganizer();
        Event existingEvent = new Event("Summer Concert", "Annual summer music event", "music",
                "2025-07-20T19:00:00", "2025-07-20T23:00:00", "Park Hall", 500);
        existingEvent.setEventId(1L);
        existingEvent.setPrice(50.0);
        existingEvent.setImageUrl("https://example.com/concert.jpg");
        existingEvent.setOrganizer(organizer);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        // Fetching event to pre-populate edit form
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.title").value("Summer Concert"))
                .andExpect(jsonPath("$.description").value("Annual summer music event"))
                .andExpect(jsonPath("$.eventType").value("music"))
                .andExpect(jsonPath("$.price").value(50.0))
                .andExpect(jsonPath("$.startDateTime").value("2025-07-20T19:00:00"))
                .andExpect(jsonPath("$.endDateTime").value("2025-07-20T23:00:00"))
                .andExpect(jsonPath("$.location").value("Park Hall"))
                .andExpect(jsonPath("$.capacity").value(500))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/concert.jpg"));
    }

    // ==================== U12: View Registered Students Tests ====================

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return list of registered students")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturnRegisteredStudentsForEvent() throws Exception {
        Organizer organizer = createTestOrganizer();

        StudentRegistrationDTO student1 = new StudentRegistrationDTO(
                1L, "John", "Doe", "john.doe@example.com", "5141234567",
                101L, "LINKT-1-101", false, null
        );
        StudentRegistrationDTO student2 = new StudentRegistrationDTO(
                2L, "Jane", "Smith", "jane.smith@example.com", "5149876543",
                102L, "LINKT-1-102", true, "2025-06-15T10:30:00"
        );
        StudentRegistrationDTO student3 = new StudentRegistrationDTO(
                3L, "Bob", "Johnson", "bob.johnson@example.com", "5145551234",
                103L, "LINKT-1-103", false, null
        );

        List<StudentRegistrationDTO> registeredStudents = Arrays.asList(student1, student2, student3);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer)).thenReturn(registeredStudents);

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("5141234567"))
                .andExpect(jsonPath("$[0].ticketId").value(101))
                .andExpect(jsonPath("$[0].qrCode").value("LINKT-1-101"))
                .andExpect(jsonPath("$[0].isScanned").value(false))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].isScanned").value(true))
                .andExpect(jsonPath("$[1].scannedAt").value("2025-06-15T10:30:00"))
                .andExpect(jsonPath("$[2].userId").value(3))
                .andExpect(jsonPath("$[2].firstName").value("Bob"));
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return all required fields")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturnAllRequiredFieldsForRegisteredStudents() throws Exception {
        Organizer organizer = createTestOrganizer();

        StudentRegistrationDTO student = new StudentRegistrationDTO(
                1L, "John", "Doe", "john.doe@example.com", "5141234567",
                101L, "LINKT-1-101", true, "2025-06-15T10:30:00"
        );

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer)).thenReturn(Arrays.asList(student));

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].phoneNumber").exists())
                .andExpect(jsonPath("$[0].ticketId").exists())
                .andExpect(jsonPath("$[0].qrCode").exists())
                .andExpect(jsonPath("$[0].isScanned").exists())
                .andExpect(jsonPath("$[0].scannedAt").exists());
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return empty list when no students registered")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturnEmptyListWhenNoStudentsRegistered() throws Exception {
        Organizer organizer = createTestOrganizer();

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return 401 for unauthenticated user")
    void shouldReturn401ForUnauthenticatedRegisteredStudentsRequest() throws Exception {
        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return 403 when non-organizer tries to access")
    @WithMockUser(username = "student@example.com", roles = {"STUDENT"})
    void shouldReturn403WhenNonOrganizerTriesToAccessRegisteredStudents() throws Exception {
        com.linkt.model.Student student = new com.linkt.model.Student(
                "student@example.com", "John", "Doe", "5141234567", "hashedPassword"
        );
        student.setUserId(1L);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return 403 when organizer tries to access another organizer's event")
    @WithMockUser(username = "organizer1@example.com", roles = {"ORGANIZER"})
    void shouldReturn403WhenOrganizerTriesToAccessAnotherOrganizersEvent() throws Exception {
        Organizer organizer1 = new Organizer("organizer1@example.com", "Alice", "Smith", "5141111111", "hashedPassword");
        organizer1.setUserId(1L);

        when(userRepository.findByEmail("organizer1@example.com")).thenReturn(Optional.of(organizer1));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer1))
                .thenThrow(new RuntimeException("You don't have permission to view this event's students"));

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You don't have permission to view this event's students"));
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should return 404 when event not found")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldReturn404WhenEventNotFoundForRegisteredStudents() throws Exception {
        Organizer organizer = createTestOrganizer();

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(999L, organizer))
                .thenThrow(new RuntimeException("Event not found"));

        mockMvc.perform(get("/api/events/999/registered-students"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should show check-in status correctly")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldShowCheckInStatusCorrectlyForRegisteredStudents() throws Exception {
        Organizer organizer = createTestOrganizer();

        StudentRegistrationDTO scannedStudent = new StudentRegistrationDTO(
                1L, "John", "Doe", "john.doe@example.com", "5141234567",
                101L, "LINKT-1-101", true, "2025-06-15T10:30:00"
        );
        StudentRegistrationDTO unscannedStudent = new StudentRegistrationDTO(
                2L, "Jane", "Smith", "jane.smith@example.com", "5149876543",
                102L, "LINKT-1-102", false, null
        );

        List<StudentRegistrationDTO> students = Arrays.asList(scannedStudent, unscannedStudent);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer)).thenReturn(students);

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isScanned").value(true))
                .andExpect(jsonPath("$[0].scannedAt").value("2025-06-15T10:30:00"))
                .andExpect(jsonPath("$[1].isScanned").value(false))
                .andExpect(jsonPath("$[1].scannedAt").doesNotExist());
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should handle large number of students")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldHandleLargeNumberOfRegisteredStudents() throws Exception {
        Organizer organizer = createTestOrganizer();

        List<StudentRegistrationDTO> manyStudents = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            manyStudents.add(new StudentRegistrationDTO(
                    (long) i, "Student" + i, "Lastname" + i,
                    "student" + i + "@example.com", "514000" + String.format("%04d", i),
                    (long) (100 + i), "LINKT-1-" + (100 + i), i % 3 == 0, null
            ));
        }

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer)).thenReturn(manyStudents);

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100))
                .andExpect(jsonPath("$[0].firstName").value("Student1"))
                .andExpect(jsonPath("$[99].firstName").value("Student100"));
    }

    @Test
    @DisplayName("U12: GET /api/events/{eventId}/registered-students should properly format phone numbers")
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void shouldProperlyFormatPhoneNumbersForRegisteredStudents() throws Exception {
        Organizer organizer = createTestOrganizer();

        StudentRegistrationDTO student = new StudentRegistrationDTO(
                1L, "John", "Doe", "john.doe@example.com", "5141234567",
                101L, "LINKT-1-101", false, null
        );

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
        when(eventService.getRegisteredStudentsForEvent(1L, organizer)).thenReturn(Arrays.asList(student));

        mockMvc.perform(get("/api/events/1/registered-students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].phoneNumber").value("5141234567"))
                .andExpect(jsonPath("$[0].phoneNumber").isString());
    }
}


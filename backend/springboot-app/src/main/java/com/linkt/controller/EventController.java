package com.linkt.controller;

import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.model.User;
import com.linkt.repository.EventRepository;
import com.linkt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/add")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Event> addEvent(@RequestBody com.linkt.dto.EventDTO eventDTO, Authentication authentication)
    {
        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username)
                                        .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!(currentUser instanceof Organizer)) {
            throw new RuntimeException("Authenticated user is not an Organizer");
        }
        Organizer organizer = (Organizer) currentUser;

        Event event = new Event();
        event.setTitle(eventDTO.getTitle());  
        event.setDescription(eventDTO.getDescription());
        event.setEventType(eventDTO.getEventType());
        event.setPrice(eventDTO.getPrice());
        event.setStartDateTime(eventDTO.getStartDateTime());
        event.setEndDateTime(eventDTO.getEndDateTime());
        event.setLocation(eventDTO.getLocation()); 
        event.setCapacity(eventDTO.getCapacity());
        event.setImageUrl(eventDTO.getImage());
        event.setOrganizer(organizer); // Set the organizer here
        
        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.status(201).body(savedEvent);
    }                                                                                                                                                                        
}                 

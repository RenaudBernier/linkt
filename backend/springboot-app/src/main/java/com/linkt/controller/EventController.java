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

import java.io.IOException;                                                                     
import java.nio.file.Files;                                                                     
import java.nio.file.Path;                                                                      
import java.nio.file.Paths;                                                                     
import java.nio.file.StandardCopyOption;                                                        
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;;

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
    public ResponseEntity<Event> addEvent(@RequestParam("title") String title, 
    @RequestParam("description") String description, @RequestParam("eventType") String eventType,        
    @RequestParam("price") double price,  @RequestParam("startDateTime") String startDateTime, @RequestParam("endDateTime") String endDateTime, 
    @RequestParam("location") String location, @RequestParam("capacity") int capacity, @RequestPart("image") MultipartFile image,
    Authentication authentication) 
    throws IOException 
    {
        String username = authentication.getName();
        User currentUser = userRepository.findByEmail(username)
                                        .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!(currentUser instanceof Organizer)) {
            throw new RuntimeException("Authenticated user is not an Organizer");
        }
        Organizer organizer = (Organizer) currentUser;

        Event event = new Event();
        event.setTitle(title);  
        event.setDescription(description);
        event.setEventType(eventType);
        event.setPrice(price);
        event.setStartDateTime(startDateTime);
        event.setEndDateTime(endDateTime);
        event.setLocation(location); 
        event.setCapacity(capacity);
        event.setImageUrl(saveImage(image));
        event.setOrganizer(organizer); // Set the organizer here
        
        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.status(201).body(savedEvent);
    }                                                                                           
    private String saveImage(MultipartFile image) throws IOException { 
        // Save the image to a directory and return the path
        // This is a simplified example; you might want to use a more robust solution   
        String fileName = StringUtils.cleanPath(image.getOriginalFilename());   
        Path path = Paths.get("uploads/" + fileName);
        Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); 
        return path.toString(); 
    }                                                                               
}                 

package com.linkt.controller;

import com.linkt.model.User;
import com.linkt.model.Organizer;
import com.linkt.repository.UserRepository;
import com.linkt.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // This endpoint should be secured to be only accessible by administrators
    @GetMapping("/pending-organizers")
    public ResponseEntity<List<Organizer>> getPendingOrganizers() {
        return ResponseEntity.ok(userService.getPendingOrganizers());
    }

    // This endpoint should be secured to be only accessible by administrators
    @PutMapping("/approve-organizer/{userId}")
    public ResponseEntity<?> approveOrganizer(@PathVariable Long userId) {
        userService.approveOrganizer(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user: " + e.getMessage());
        }
    }
}

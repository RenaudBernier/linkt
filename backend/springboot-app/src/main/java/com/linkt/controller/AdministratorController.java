package com.linkt.controller;

import com.linkt.dto.AdministratorDTO;
import com.linkt.dto.GlobalStatsResponse;
import com.linkt.service.AdministratorService;
import com.linkt.service.GlobalStatsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administrators")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdministratorController {

    @Autowired
    private AdministratorService administratorService;

    @Autowired
    private GlobalStatsService globalStatsService;

    // Get global statistics (admin only)
    @GetMapping("/stats/global")
    public ResponseEntity<GlobalStatsResponse> getGlobalStatistics() {
        try {
            GlobalStatsResponse stats = globalStatsService.getGlobalStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Create administrator (promote user to admin)
    @PostMapping
    public ResponseEntity<AdministratorDTO> createAdministrator(@Valid @RequestBody AdministratorDTO dto) {
        try {
            AdministratorDTO created = administratorService.createAdministrator(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Get all administrators
    @GetMapping
    public ResponseEntity<List<AdministratorDTO>> getAllAdministrators() {
        List<AdministratorDTO> admins = administratorService.getAllAdministrators();
        return new ResponseEntity<>(admins, HttpStatus.OK);
    }

    // Get administrator by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdministratorDTO> getAdministratorById(@PathVariable Long id) {
        try {
            AdministratorDTO admin = administratorService.getAdministratorById(id);
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Get administrator by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<AdministratorDTO> getAdministratorByUserId(@PathVariable Long userId) {
        try {
            AdministratorDTO admin = administratorService.getAdministratorByUserId(userId);
            return new ResponseEntity<>(admin, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Check if user is an administrator
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> isAdministrator(@PathVariable Long userId) {
        boolean isAdmin = administratorService.isAdministrator(userId);
        return new ResponseEntity<>(isAdmin, HttpStatus.OK);
    }

    // Delete administrator (remove admin privileges)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdministrator(@PathVariable Long id) {
        try {
            administratorService.deleteAdministrator(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
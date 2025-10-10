package com.linkt.service;

import com.linkt.dto.AuthResponse;
import com.linkt.dto.LoginRequest;
import com.linkt.dto.RegisterRequest;
import com.linkt.repository.UserRepository;
import com.linkt.security.JwtUtil;
import com.model.Organizer;
import com.model.Student;
import com.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPassword().length() < 7) {
            throw new RuntimeException("Password must be at least 7 characters");
        }

        User user;
        if ("student".equalsIgnoreCase(request.getUserType())) {
            user = new Student(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    passwordEncoder.encode(request.getPassword())
            );
        } else if ("organizer".equalsIgnoreCase(request.getUserType())) {
            if (request.getOrganizationName() == null || request.getOrganizationName().trim().isEmpty()) {
                throw new RuntimeException("Organization name is required for organizers");
            }
            Organizer organizer = new Organizer(
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    passwordEncoder.encode(request.getPassword())
            );
            organizer.setOrganizationName(request.getOrganizationName());
            user = organizer;
        } else {
            throw new RuntimeException("Invalid user type. Must be 'student' or 'organizer'");
        }

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserType());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUserType()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getUserType());

        return new AuthResponse(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUserType()
        );
    }
}

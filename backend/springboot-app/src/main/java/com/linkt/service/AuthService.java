package com.linkt.service;

import com.linkt.dto.AuthResponse;
import com.linkt.dto.LoginRequest;
import com.linkt.dto.RegisterRequest;
import com.linkt.dto.VerifyCodeRequest;
import com.linkt.repository.UserRepository;
import com.linkt.security.JwtUtil;
import com.linkt.model.Organizer;
import com.linkt.model.Student;
import com.linkt.model.User;
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

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private EmailService emailService;

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

        // Generate and store verification code
        String code = verificationService.generateCode();
        user.setEmailVerified(false);
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(verificationService.getCodeExpiry());

        user = userRepository.save(user);

        // Send verification code via email
        emailService.sendVerificationCode(user.getEmail(), user.getFirstName(), code);

        // Return response WITHOUT token
        return new AuthResponse(
                null,  // No token
                user.getUserId(),
                user.getEmail(),
                null,
                null,
                null,
                "EMAIL_VERIFICATION_REQUIRED"
        );
    }

    public AuthResponse verifyEmail(VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        if (!verificationService.isCodeValid(
                user.getVerificationCode(),
                user.getVerificationCodeExpiry(),
                request.getCode())) {
            throw new RuntimeException("Invalid or expired verification code");
        }

        // Activate account
        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        // Generate JWT token
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
        // Verify credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Skip 2FA for administrators - they get immediate access
        if ("administrator".equalsIgnoreCase(user.getUserType())) {
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

        // Check if email is verified for students and organizers
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        // Generate and store 2FA code for students and organizers
        String code = verificationService.generateCode();
        user.setTwoFactorCode(code);
        user.setTwoFactorCodeExpiry(verificationService.getCodeExpiry());
        userRepository.save(user);

        // Send 2FA code via email
        emailService.send2FACode(user.getEmail(), user.getFirstName(), code);

        // Return response WITHOUT token
        return new AuthResponse(
                null,  // No token
                user.getUserId(),
                user.getEmail(),
                null,
                null,
                null,
                "2FA_REQUIRED"
        );
    }

    public AuthResponse verify2FA(VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!verificationService.isCodeValid(
                user.getTwoFactorCode(),
                user.getTwoFactorCodeExpiry(),
                request.getCode())) {
            throw new RuntimeException("Invalid or expired 2FA code");
        }

        // Clear 2FA code
        user.setTwoFactorCode(null);
        user.setTwoFactorCodeExpiry(null);
        userRepository.save(user);

        // Generate JWT token
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

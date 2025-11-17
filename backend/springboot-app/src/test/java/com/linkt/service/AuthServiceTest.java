package com.linkt.service;

import com.linkt.dto.*;
import com.linkt.model.*;
import com.linkt.repository.UserRepository;
import com.linkt.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private VerificationService verificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Student testStudent;
    private Administrator testAdmin;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPassword("password123");
        registerRequest.setUserType("student");
        registerRequest.setPhoneNumber("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testStudent = new Student("test@example.com", "John", "Doe", "1234567890", "hashedPassword");
        testStudent.setEmailVerified(true);

        testAdmin = new Administrator();
        testAdmin.setEmail("admin@linkt.dev");
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setPassword("hashedPassword");
    }

    // Registration tests

    @Test
    @DisplayName("Should register new student and send verification code")
    void shouldRegisterNewStudentAndSendVerificationCode() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(verificationService.generateCode()).thenReturn("123456");
        when(verificationService.getCodeExpiry()).thenReturn(LocalDateTime.now().plusMinutes(5));
        when(userRepository.save(any(Student.class))).thenReturn(testStudent);

        AuthResponse response = authService.register(registerRequest);

        assertNull(response.getToken(), "Token should be null before email verification");
        assertEquals("EMAIL_VERIFICATION_REQUIRED", response.getUserType());
        assertEquals("test@example.com", response.getEmail());

        verify(emailService).sendVerificationCode(eq("test@example.com"), eq("John"), eq("123456"));
        verify(userRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Should reject registration with existing email")
    void shouldRejectRegistrationWithExistingEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration with short password")
    void shouldRejectRegistrationWithShortPassword() {
        registerRequest.setPassword("short");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));

        assertEquals("Password must be at least 7 characters", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register organizer with organization name")
    void shouldRegisterOrganizerWithOrganizationName() {
        registerRequest.setUserType("organizer");
        registerRequest.setOrganizationName("Test Org");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(verificationService.generateCode()).thenReturn("123456");
        when(verificationService.getCodeExpiry()).thenReturn(LocalDateTime.now().plusMinutes(5));

        Organizer testOrganizer = new Organizer("test@example.com", "John", "Doe", "1234567890", "hashedPassword");
        testOrganizer.setOrganizationName("Test Org");
        when(userRepository.save(any(Organizer.class))).thenReturn(testOrganizer);

        AuthResponse response = authService.register(registerRequest);

        assertEquals("EMAIL_VERIFICATION_REQUIRED", response.getUserType());
        verify(userRepository).save(any(Organizer.class));
    }

    @Test
    @DisplayName("Should reject organizer registration without organization name")
    void shouldRejectOrganizerRegistrationWithoutOrganizationName() {
        registerRequest.setUserType("organizer");
        registerRequest.setOrganizationName(null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));

        assertEquals("Organization name is required for organizers", exception.getMessage());
    }

    // Email verification tests

    @Test
    @DisplayName("Should verify email with valid code and return JWT token")
    void shouldVerifyEmailWithValidCode() {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "123456");
        testStudent.setEmailVerified(false);
        testStudent.setVerificationCode("123456");
        testStudent.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));
        when(verificationService.isCodeValid(anyString(), any(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        AuthResponse response = authService.verifyEmail(request);

        assertNotNull(response.getToken(), "Token should be returned after verification");
        assertEquals("jwt-token", response.getToken());
        assertEquals("student", response.getUserType());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject email verification with invalid code")
    void shouldRejectEmailVerificationWithInvalidCode() {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "wrong-code");
        testStudent.setEmailVerified(false);
        testStudent.setVerificationCode("123456");
        testStudent.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));
        when(verificationService.isCodeValid(anyString(), any(), anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.verifyEmail(request));

        assertEquals("Invalid or expired verification code", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject verification for already verified email")
    void shouldRejectVerificationForAlreadyVerifiedEmail() {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "123456");
        testStudent.setEmailVerified(true);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.verifyEmail(request));

        assertEquals("Email already verified", exception.getMessage());
        verify(verificationService, never()).isCodeValid(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should reject verification for non-existent user")
    void shouldRejectVerificationForNonExistentUser() {
        VerifyCodeRequest request = new VerifyCodeRequest("nonexistent@example.com", "123456");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.verifyEmail(request));

        assertEquals("User not found", exception.getMessage());
    }

    // Login tests

    @Test
    @DisplayName("Should allow admin to login without 2FA")
    void shouldAllowAdminLoginWithout2FA() {
        loginRequest.setEmail("admin@linkt.dev");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail("admin@linkt.dev")).thenReturn(Optional.of(testAdmin));
        when(jwtUtil.generateToken("admin@linkt.dev", "administrator")).thenReturn("admin-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response.getToken(), "Admin should get token immediately");
        assertEquals("admin-jwt-token", response.getToken());
        assertEquals("administrator", response.getUserType());
        assertEquals("Admin", response.getFirstName());

        verify(emailService, never()).send2FACode(anyString(), anyString(), anyString());
        verify(verificationService, never()).generateCode();
    }

    @Test
    @DisplayName("Should require 2FA for student login")
    void shouldRequire2FAForStudentLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));
        when(verificationService.generateCode()).thenReturn("654321");
        when(verificationService.getCodeExpiry()).thenReturn(LocalDateTime.now().plusMinutes(5));
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        AuthResponse response = authService.login(loginRequest);

        assertNull(response.getToken(), "Student should not get token before 2FA");
        assertEquals("2FA_REQUIRED", response.getUserType());
        assertEquals("test@example.com", response.getEmail());

        verify(emailService).send2FACode(eq("test@example.com"), eq("John"), eq("654321"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should require 2FA for organizer login")
    void shouldRequire2FAForOrganizerLogin() {
        Organizer testOrganizer = new Organizer("org@example.com", "Jane", "Smith", "9876543210", "hashedPassword");
        testOrganizer.setEmailVerified(true);

        loginRequest.setEmail("org@example.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail("org@example.com")).thenReturn(Optional.of(testOrganizer));
        when(verificationService.generateCode()).thenReturn("111222");
        when(verificationService.getCodeExpiry()).thenReturn(LocalDateTime.now().plusMinutes(5));
        when(userRepository.save(any(User.class))).thenReturn(testOrganizer);

        AuthResponse response = authService.login(loginRequest);

        assertNull(response.getToken());
        assertEquals("2FA_REQUIRED", response.getUserType());
        verify(emailService).send2FACode(eq("org@example.com"), eq("Jane"), eq("111222"));
    }

    @Test
    @DisplayName("Should reject login for unverified email")
    void shouldRejectLoginForUnverifiedEmail() {
        testStudent.setEmailVerified(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.login(loginRequest));

        assertEquals("Please verify your email before logging in", exception.getMessage());
        verify(verificationService, never()).generateCode();
    }

    @Test
    @DisplayName("Should reject login with bad credentials")
    void shouldRejectLoginWithBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));

        verify(userRepository, never()).findByEmail(anyString());
    }

    // 2FA verification tests

    @Test
    @DisplayName("Should verify 2FA code and return JWT token")
    void shouldVerify2FACodeAndReturnJWT() {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "654321");
        testStudent.setTwoFactorCode("654321");
        testStudent.setTwoFactorCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));
        when(verificationService.isCodeValid(anyString(), any(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("2fa-jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(testStudent);

        AuthResponse response = authService.verify2FA(request);

        assertNotNull(response.getToken());
        assertEquals("2fa-jwt-token", response.getToken());
        assertEquals("student", response.getUserType());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject invalid 2FA code")
    void shouldRejectInvalid2FACode() {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "wrong");
        testStudent.setTwoFactorCode("654321");
        testStudent.setTwoFactorCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));
        when(verificationService.isCodeValid(anyString(), any(), anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.verify2FA(request));

        assertEquals("Invalid or expired 2FA code", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject 2FA verification for non-existent user")
    void shouldReject2FAVerificationForNonExistentUser() {
        VerifyCodeRequest request = new VerifyCodeRequest("nonexistent@example.com", "654321");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.verify2FA(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should clear 2FA code after successful verification")
    void shouldClear2FACodeAfterSuccessfulVerification() {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "654321");
        testStudent.setTwoFactorCode("654321");
        testStudent.setTwoFactorCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testStudent));
        when(verificationService.isCodeValid(anyString(), any(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNull(savedUser.getTwoFactorCode(), "2FA code should be cleared");
            assertNull(savedUser.getTwoFactorCodeExpiry(), "2FA expiry should be cleared");
            return savedUser;
        });

        authService.verify2FA(request);

        verify(userRepository).save(any(User.class));
    }
}

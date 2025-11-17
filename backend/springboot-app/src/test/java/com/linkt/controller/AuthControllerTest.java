package com.linkt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkt.dto.*;
import com.linkt.linkt.LinktApplication;
import com.linkt.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LinktApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Registration tests

    @Test
    @DisplayName("POST /api/auth/register should return 200 and EMAIL_VERIFICATION_REQUIRED")
    void shouldReturnEmailVerificationRequired() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setUserType("student");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("1234567890");

        AuthResponse response = new AuthResponse(
            null, 1L, "test@example.com", null, null, null, "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userType").value("EMAIL_VERIFICATION_REQUIRED"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 when email already exists")
    void shouldReturn400WhenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setUserType("student");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 for short password")
    void shouldReturn400ForShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("short");
        request.setUserType("student");
        request.setFirstName("John");
        request.setLastName("Doe");

        // This test validates that short passwords are rejected by validation
        // Validation happens before the service layer, so we just check the status
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Email verification tests

    @Test
    @DisplayName("POST /api/auth/verify-email should return 200 and JWT token")
    void shouldReturnJWTAfterEmailVerification() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "123456");
        AuthResponse response = new AuthResponse(
            "jwt-token-here", 1L, "test@example.com", "John", "Doe", "1234567890", "student"
        );

        when(authService.verifyEmail(any(VerifyCodeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.userType").value("student"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-email should return 400 for invalid code")
    void shouldReturn400ForInvalidVerificationCode() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "999999");

        when(authService.verifyEmail(any(VerifyCodeRequest.class)))
            .thenThrow(new RuntimeException("Invalid or expired verification code"));

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired verification code"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-email should return 400 for already verified email")
    void shouldReturn400ForAlreadyVerifiedEmail() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "123456");

        when(authService.verifyEmail(any(VerifyCodeRequest.class)))
            .thenThrow(new RuntimeException("Email already verified"));

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already verified"));
    }

    // Login tests

    @Test
    @DisplayName("POST /api/auth/login should return 200 and 2FA_REQUIRED for students")
    void shouldReturn2FARequiredForStudents() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse(
            null, 1L, "test@example.com", null, null, null, "2FA_REQUIRED"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.userType").value("2FA_REQUIRED"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 and JWT for administrators")
    void shouldReturnJWTForAdministrators() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@linkt.dev");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse(
            "admin-jwt-token", 7L, "admin@linkt.dev", "Admin", "User", "+1-555-0110", "administrator"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-jwt-token"))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.email").value("admin@linkt.dev"))
                .andExpect(jsonPath("$.userType").value("administrator"))
                .andExpect(jsonPath("$.firstName").value("Admin"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 for bad credentials")
    void shouldReturn401ForBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 for unverified email")
    void shouldReturn401ForUnverifiedEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unverified@example.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Please verify your email before logging in"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Please verify your email before logging in"));
    }

    // 2FA verification tests

    @Test
    @DisplayName("POST /api/auth/verify-2fa should return 200 and JWT token")
    void shouldReturnJWTAfter2FAVerification() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "654321");
        AuthResponse response = new AuthResponse(
            "2fa-jwt-token", 1L, "test@example.com", "John", "Doe", "1234567890", "student"
        );

        when(authService.verify2FA(any(VerifyCodeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("2fa-jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.userType").value("student"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-2fa should return 401 for invalid 2FA code")
    void shouldReturn401ForInvalid2FACode() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "888888");

        when(authService.verify2FA(any(VerifyCodeRequest.class)))
            .thenThrow(new RuntimeException("Invalid or expired 2FA code"));

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid or expired 2FA code"));
    }

    @Test
    @DisplayName("POST /api/auth/verify-2fa should return 401 for non-existent user")
    void shouldReturn401ForNonExistentUserIn2FA() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("nonexistent@example.com", "123456");

        when(authService.verify2FA(any(VerifyCodeRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    // Validation tests

    @Test
    @DisplayName("POST /api/auth/verify-email should validate request body")
    void shouldValidateVerifyEmailRequestBody() throws Exception {
        String invalidJson = "{\"email\": \"\", \"code\": \"12\"}"; // Empty email, short code

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/verify-2fa should validate code format")
    void shouldValidateCodeFormat() throws Exception {
        String invalidJson = "{\"email\": \"test@example.com\", \"code\": \"abc\"}"; // Non-numeric code

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}

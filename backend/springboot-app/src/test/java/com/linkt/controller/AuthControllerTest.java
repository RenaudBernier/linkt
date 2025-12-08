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

    // ==================== U4: Student Account Creation Tests ====================

    @Test
    @DisplayName("U4: POST /api/auth/register should create student account with valid data")
    void shouldCreateStudentAccountWithValidData() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.student@university.edu");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUserType("student");
        request.setPhoneNumber("1234567890");

        AuthResponse response = new AuthResponse(
            null, 1L, "john.student@university.edu", "John", "Smith", "1234567890", "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.student@university.edu"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
                .andExpect(jsonPath("$.userType").value("EMAIL_VERIFICATION_REQUIRED"));
    }

    @Test
    @DisplayName("U4: Student registration should validate email format")
    void shouldValidateEmailFormat() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUserType("student");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U4: Student registration should require minimum password length")
    void shouldRequireMinimumPasswordLength() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@university.edu");
        request.setPassword("short");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUserType("student");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U4: Student registration should require first name")
    void shouldRequireFirstName() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@university.edu");
        request.setPassword("SecurePass123");
        request.setFirstName("");
        request.setLastName("Smith");
        request.setUserType("student");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U4: Student registration should require last name")
    void shouldRequireLastName() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@university.edu");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setLastName("");
        request.setUserType("student");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U4: Student registration should validate user type")
    void shouldValidateUserType() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@university.edu");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUserType("invalid_type");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U4: Should prevent duplicate email registration")
    void shouldPreventDuplicateEmailRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@university.edu");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUserType("student");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    @DisplayName("U4: Email verification should return JWT token for student")
    void shouldReturnJWTAfterStudentEmailVerification() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("john.student@university.edu", "123456");
        AuthResponse response = new AuthResponse(
            "student-jwt-token", 1L, "john.student@university.edu", "John", "Smith", "1234567890", "student"
        );

        when(authService.verifyEmail(any(VerifyCodeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("student-jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.student@university.edu"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.userType").value("student"));
    }

    @Test
    @DisplayName("U4: Complete student registration flow - register, verify, get JWT")
    void shouldCompleteStudentRegistrationFlow() throws Exception {
        // Step 1: Register
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("john.student@university.edu");
        registerRequest.setPassword("SecurePass123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Smith");
        registerRequest.setUserType("student");
        registerRequest.setPhoneNumber("1234567890");

        AuthResponse registerResponse = new AuthResponse(
            null, 1L, "john.student@university.edu", null, null, null, "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("EMAIL_VERIFICATION_REQUIRED"));

        // Step 2: Verify email
        VerifyCodeRequest verifyRequest = new VerifyCodeRequest("john.student@university.edu", "123456");
        AuthResponse verifyResponse = new AuthResponse(
            "jwt-token-here", 1L, "john.student@university.edu", "John", "Smith", "1234567890", "student"
        );

        when(authService.verifyEmail(any(VerifyCodeRequest.class))).thenReturn(verifyResponse);

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.userType").value("student"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("U4: Student account should store all required fields")
    void shouldStoreAllRequiredFields() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@university.edu");
        request.setPassword("SecurePass123");
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setUserType("student");
        request.setPhoneNumber("555-1234");

        AuthResponse response = new AuthResponse(
            null, 1L, "john@university.edu", "John", "Smith", "555-1234", "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@university.edu"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("555-1234"));
    }

    // ==================== General Registration Tests ====================

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

    // ==================== U5: Student Login Tests ====================

    @Test
    @DisplayName("U5: POST /api/auth/login should authenticate student with valid credentials")
    void shouldAuthenticateStudentWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.student@university.edu");
        request.setPassword("SecurePass123");

        AuthResponse response = new AuthResponse(
            null, 1L, "john.student@university.edu", null, null, null, "2FA_REQUIRED"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.student@university.edu"))
                .andExpect(jsonPath("$.userType").value("2FA_REQUIRED"));
    }

    @Test
    @DisplayName("U5: Student login should require email field")
    void shouldRequireEmailForLogin() throws Exception {
        String invalidJson = "{\"password\": \"SecurePass123\"}"; // Missing email

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U5: Student login should require password field")
    void shouldRequirePasswordForLogin() throws Exception {
        String invalidJson = "{\"email\": \"john@university.edu\"}"; // Missing password

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U5: Student login should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@university.edu");
        request.setPassword("WrongPassword");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }

    @Test
    @DisplayName("U5: Student login should require email verification")
    void shouldRequireEmailVerificationForLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unverified@university.edu");
        request.setPassword("SecurePass123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Please verify your email before logging in"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Please verify your email before logging in"));
    }

    @Test
    @DisplayName("U5: Complete student login flow with 2FA should return JWT token")
    void shouldCompleteStudentLoginFlowWith2FA() throws Exception {
        // Step 1: Login with credentials
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.student@university.edu");
        loginRequest.setPassword("SecurePass123");

        AuthResponse loginResponse = new AuthResponse(
            null, 1L, "john.student@university.edu", null, null, null, "2FA_REQUIRED"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("2FA_REQUIRED"))
                .andExpect(jsonPath("$.token").doesNotExist());

        // Step 2: Verify 2FA code
        VerifyCodeRequest verify2FARequest = new VerifyCodeRequest("john.student@university.edu", "123456");
        AuthResponse verify2FAResponse = new AuthResponse(
            "student-jwt-token", 1L, "john.student@university.edu", "John", "Smith", "1234567890", "student"
        );

        when(authService.verify2FA(any(VerifyCodeRequest.class))).thenReturn(verify2FAResponse);

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verify2FARequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("student-jwt-token"))
                .andExpect(jsonPath("$.userType").value("student"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("john.student@university.edu"));
    }

    @Test
    @DisplayName("U5: Student login should return user information after successful authentication")
    void shouldReturnUserInfoAfterSuccessfulLogin() throws Exception {
        VerifyCodeRequest request = new VerifyCodeRequest("john@university.edu", "123456");
        AuthResponse response = new AuthResponse(
            "jwt-token", 1L, "john@university.edu", "John", "Smith", "1234567890", "student"
        );

        when(authService.verify2FA(any(VerifyCodeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john@university.edu"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.userType").value("student"));
    }

    @Test
    @DisplayName("U5: Student login should reject non-existent user")
    void shouldRejectNonExistentUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@university.edu");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }

    @Test
    @DisplayName("U5: Student should be able to access protected resources after login")
    void shouldAccessProtectedResourcesAfterLogin() throws Exception {
        // This test verifies that after successful 2FA, student receives JWT token
        // which can be used to access protected endpoints
        VerifyCodeRequest request = new VerifyCodeRequest("john@university.edu", "123456");
        AuthResponse response = new AuthResponse(
            "valid-jwt-token", 1L, "john@university.edu", "John", "Smith", "1234567890", "student"
        );

        when(authService.verify2FA(any(VerifyCodeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/verify-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("valid-jwt-token"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("U5: Failed login attempts should not expose whether user exists")
    void shouldNotExposeUserExistence() throws Exception {
        LoginRequest request1 = new LoginRequest();
        request1.setEmail("nonexistent@university.edu");
        request1.setPassword("password123");

        LoginRequest request2 = new LoginRequest();
        request2.setEmail("exists@university.edu");
        request2.setPassword("wrongpassword");

        // Both should return the same generic error message
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }

    // ==================== General Login Tests ====================

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

    // ==================== U13: Organizer Login and Registration Tests ====================

    @Test
    @DisplayName("U13: POST /api/auth/register should create organizer account with valid data")
    void shouldCreateOrganizerAccountWithValidData() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("SecurePass123");
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        AuthResponse response = new AuthResponse(
            null, 1L, "organizer@company.com", "Alice", "Johnson", "5141234567", "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("organizer@company.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.phoneNumber").value("5141234567"))
                .andExpect(jsonPath("$.userType").value("EMAIL_VERIFICATION_REQUIRED"));
    }

    @Test
    @DisplayName("U13: Organizer registration should validate email format")
    void shouldValidateOrganizerEmailFormat() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email-format");
        request.setPassword("SecurePass123");
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U13: Organizer registration should require minimum password length")
    void shouldRequireMinimumPasswordLengthForOrganizer() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("short");
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U13: Organizer registration should require first name")
    void shouldRequireFirstNameForOrganizer() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("SecurePass123");
        request.setFirstName("");
        request.setLastName("Johnson");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U13: Organizer registration should require last name")
    void shouldRequireLastNameForOrganizer() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("SecurePass123");
        request.setFirstName("Alice");
        request.setLastName("");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U13: Organizer registration should reject duplicate email")
    void shouldRejectDuplicateOrganizerEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@company.com");
        request.setPassword("SecurePass123");
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    @DisplayName("U13: Organizer registration should require userType to be 'organizer'")
    void shouldRequireOrganizerUserType() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("SecurePass123");
        request.setFirstName("Alice");
        request.setLastName("Johnson");
        request.setUserType("organizer");
        request.setPhoneNumber("5141234567");

        AuthResponse response = new AuthResponse(
            null, 1L, "organizer@company.com", "Alice", "Johnson", "5141234567", "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("EMAIL_VERIFICATION_REQUIRED"));
    }

    @Test
    @DisplayName("U13: POST /api/auth/login should authenticate organizer with valid credentials")
    void shouldAuthenticateOrganizerWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("SecurePass123");

        AuthResponse response = new AuthResponse(
            "jwt-token-here", 1L, "organizer@company.com", "Alice", "Johnson", "5141234567", "organizer"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("organizer@company.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.userType").value("organizer"))
                .andExpect(jsonPath("$.token").value("jwt-token-here"));
    }

    @Test
    @DisplayName("U13: Organizer login should require email field")
    void shouldRequireEmailForOrganizerLogin() throws Exception {
        String invalidJson = "{\"password\": \"SecurePass123\"}"; // Missing email

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U13: Organizer login should require password field")
    void shouldRequirePasswordForOrganizerLogin() throws Exception {
        String invalidJson = "{\"email\": \"organizer@company.com\"}"; // Missing password

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("U13: Organizer login should reject invalid credentials")
    void shouldRejectInvalidOrganizerCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("WrongPassword");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }

    @Test
    @DisplayName("U13: Organizer login should reject unverified email")
    void shouldRejectUnverifiedOrganizerEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("unverified@company.com");
        request.setPassword("SecurePass123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Please verify your email before logging in"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Please verify your email before logging in"));
    }

    @Test
    @DisplayName("U13: Organizer login should return JWT token on successful authentication")
    void shouldReturnJwtTokenForOrganizer() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("organizer@company.com");
        request.setPassword("SecurePass123");

        AuthResponse response = new AuthResponse(
            "jwt-token-12345", 1L, "organizer@company.com", "Alice", "Johnson", "5141234567", "organizer"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value("jwt-token-12345"))
                .andExpect(jsonPath("$.userType").value("organizer"));
    }

    @Test
    @DisplayName("U13: Organizer login should handle non-existent user")
    void shouldHandleNonExistentOrganizer() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@company.com");
        request.setPassword("SecurePass123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));
    }

    @Test
    @DisplayName("U13: Successful organizer registration should redirect to verification")
    void shouldRedirectToVerificationAfterOrganizerRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("neworganizer@company.com");
        request.setPassword("SecurePass123");
        request.setFirstName("Bob");
        request.setLastName("Smith");
        request.setUserType("organizer");
        request.setPhoneNumber("5149876543");

        AuthResponse response = new AuthResponse(
            null, 2L, "neworganizer@company.com", "Bob", "Smith", "5149876543", "EMAIL_VERIFICATION_REQUIRED"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("EMAIL_VERIFICATION_REQUIRED"))
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    @DisplayName("U13: Successful organizer login should return user details with organizer role")
    void shouldReturnOrganizerDetailsOnSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("verified@company.com");
        request.setPassword("SecurePass123");

        AuthResponse response = new AuthResponse(
            "jwt-token-verified", 3L, "verified@company.com", "Charlie", "Brown", "5145551234", "organizer"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.email").value("verified@company.com"))
                .andExpect(jsonPath("$.firstName").value("Charlie"))
                .andExpect(jsonPath("$.lastName").value("Brown"))
                .andExpect(jsonPath("$.phoneNumber").value("5145551234"))
                .andExpect(jsonPath("$.userType").value("organizer"))
                .andExpect(jsonPath("$.token").value("jwt-token-verified"));
    }
}

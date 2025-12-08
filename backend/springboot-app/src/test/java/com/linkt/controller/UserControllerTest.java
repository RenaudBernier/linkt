package com.linkt.controller;

import com.linkt.linkt.LinktApplication;
import com.linkt.model.Student;
import com.linkt.model.Organizer;
import com.linkt.repository.UserRepository;
import com.linkt.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LinktApplication.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    // ==================== U6: Dropdown Menu Navigation Tests ====================

    @Test
    @DisplayName("U6: GET /api/users/me should return authenticated student profile for dropdown menu")
    @WithMockUser(username = "john.student@university.edu")
    void shouldReturnStudentProfileForDropdownMenu() throws Exception {
        Student student = new Student("john.student@university.edu", "John", "Smith", "1234567890", "hashedPassword");
        student.setUserId(1L);

        when(userRepository.findByEmail("john.student@university.edu")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("john.student@university.edu"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    @DisplayName("U6: GET /api/users/me should return 403 for unauthenticated user accessing dropdown")
    void shouldReturn403ForUnauthenticatedUserDropdown() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("U6: Dropdown menu should display student's full name")
    @WithMockUser(username = "jane.doe@university.edu")
    void shouldDisplayStudentFullName() throws Exception {
        Student student = new Student("jane.doe@university.edu", "Jane", "Doe", "5555555555", "hashedPassword");
        student.setUserId(2L);

        when(userRepository.findByEmail("jane.doe@university.edu")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("U6: Dropdown menu should display organizer profile")
    @WithMockUser(username = "organizer@company.com")
    void shouldDisplayOrganizerProfile() throws Exception {
        Organizer organizer = new Organizer("organizer@company.com", "Alice", "Johnson", "9999999999", "hashedPassword");
        organizer.setUserId(3L);
        organizer.setOrganizationName("Tech Events Inc.");

        when(userRepository.findByEmail("organizer@company.com")).thenReturn(Optional.of(organizer));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.email").value("organizer@company.com"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.organizationName").value("Tech Events Inc."));
    }

    @Test
    @DisplayName("U6: Should return 500 when user profile not found in database")
    @WithMockUser(username = "deleted@university.edu")
    void shouldReturn500WhenUserNotFound() throws Exception {
        when(userRepository.findByEmail("deleted@university.edu")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("U6: Dropdown menu should include user email for display")
    @WithMockUser(username = "test.user@university.edu")
    void shouldIncludeUserEmail() throws Exception {
        Student student = new Student("test.user@university.edu", "Test", "User", "1111111111", "hashedPassword");
        student.setUserId(4L);

        when(userRepository.findByEmail("test.user@university.edu")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test.user@university.edu"))
                .andExpect(jsonPath("$.email").isNotEmpty());
    }

    @Test
    @DisplayName("U6: Dropdown menu should provide user ID for navigation")
    @WithMockUser(username = "user@example.com")
    void shouldProvideUserIdForNavigation() throws Exception {
        Student student = new Student("user@example.com", "Nav", "User", "2222222222", "hashedPassword");
        student.setUserId(99L);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(99))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    @DisplayName("U6: Multiple authenticated users should each get their own profile")
    @WithMockUser(username = "user1@example.com")
    void shouldReturnCorrectProfileForEachUser() throws Exception {
        Student student1 = new Student("user1@example.com", "User", "One", "3333333333", "hashedPassword");
        student1.setUserId(10L);

        Student student2 = new Student("user2@example.com", "User", "Two", "4444444444", "hashedPassword");
        student2.setUserId(11L);

        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(student1));
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(student2));

        // Test user1
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.firstName").value("User"))
                .andExpect(jsonPath("$.lastName").value("One"));
    }

    @Test
    @DisplayName("U6: Dropdown should handle users with special characters in name")
    @WithMockUser(username = "special@example.com")
    void shouldHandleSpecialCharactersInName() throws Exception {
        Student student = new Student("special@example.com", "José", "O'Brien-Smith", "5555555555", "hashedPassword");
        student.setUserId(12L);

        when(userRepository.findByEmail("special@example.com")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("José"))
                .andExpect(jsonPath("$.lastName").value("O'Brien-Smith"));
    }

    @Test
    @DisplayName("U6: Dropdown should return all necessary user information for profile display")
    @WithMockUser(username = "complete@example.com")
    void shouldReturnCompleteUserInformation() throws Exception {
        Student student = new Student("complete@example.com", "Complete", "Profile", "9876543210", "hashedPassword");
        student.setUserId(20L);

        when(userRepository.findByEmail("complete@example.com")).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.phoneNumber").exists());
    }
}

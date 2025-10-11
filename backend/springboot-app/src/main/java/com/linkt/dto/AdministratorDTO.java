package com.linkt.dto;

import jakarta.validation.constraints.NotNull;

public class AdministratorDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String username;
    private String email;

    public AdministratorDTO() {}

    public AdministratorDTO(Long id, Long userId, String username, String email) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.email = email;

    }
     public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
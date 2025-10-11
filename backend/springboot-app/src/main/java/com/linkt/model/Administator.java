package com.linkt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "administrators")

public class Administrator extends User {

    @Id
    @GeneratedValue(stategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @NotNull(message = "User is required")
    private User user;

    public Administrator() { super(); }
    public Administrator(User user) {
        this.user = user;
    }
}
package com.linkt.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("administrator")
public class Administrator extends User {

    public Administrator() {
        super();
    }

    public Administrator(String email, String firstName, String lastName, String phoneNumber, String password) {
        super(email, firstName, lastName, phoneNumber, password);
    }
}

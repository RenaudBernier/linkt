package com.linkt.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("administrator")
public class Administrator extends User {

    public Administrator() {
        super();
    }
}
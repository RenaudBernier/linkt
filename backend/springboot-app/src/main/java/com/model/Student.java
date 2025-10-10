package com.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("student")
public class Student extends User
{
    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    private List<SavedEvent> savedEvents = new ArrayList<>();

    public Student() { super();}

    public Student(String email, String firstName, String lastName, String phoneNumber, String password)
    {
        super(email, firstName, lastName, phoneNumber, password);
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<SavedEvent> getSavedEvents() {
        return savedEvents;
    }

    public void setSavedEvents(List<SavedEvent> savedEvents) {
        this.savedEvents = savedEvents;
    }
}

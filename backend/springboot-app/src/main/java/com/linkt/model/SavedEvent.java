package com.linkt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "saved_events")
public class SavedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedEventId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    public SavedEvent() {}
    public SavedEvent(Student student, Event event) {
        this.student = student;
        this.event = event;
    }

    public Long getSavedEventId() {
        return savedEventId;
    }
    
    public void setSavedEventId(Long savedEventId) {
        this.savedEventId = savedEventId;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Event getEvent() {
        return event;
    }
    
    public void setEvent(Event event) {
        this.event = event;
    }
}
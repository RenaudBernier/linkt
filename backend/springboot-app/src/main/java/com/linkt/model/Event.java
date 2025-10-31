package com.linkt.model;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "start_date_time")
    private String startDateTime;

    @Column(name = "end_date_time")
    private String endDateTime;

    private String location;
    private String coordinates;
    private int capacity;

    @Column(name = "image_url")
    private String imageUrl;

    private double price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.PUBLISHED;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SavedEvent> savedByStudents = new ArrayList<>();

    public Event() {}
    public Event(String title, String description, String eventType, String startDateTime,String endDateTime, String location, int capacity) {
        this.title = title;
        this.description = description;
        this.eventType = eventType;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.capacity = capacity;
    }
public Long getEventId() {
        return eventId;
    }
    
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getStartDateTime() {
        return startDateTime;
    }
    
    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }
    
    public String getEndDateTime() {
        return endDateTime;
    }
    
    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public int getCapacity() {
        return capacity;
    }
    
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Organizer getOrganizer() {
        return organizer;
    }
    
    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }
    
    public List<Ticket> getTickets() {
        return tickets;
    }
    
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
    
    public List<SavedEvent> getSavedByStudents() {
        return savedByStudents;
    }
    
    public void setSavedByStudents(List<SavedEvent> savedByStudents) {
        this.savedByStudents = savedByStudents;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    // Statistics methods
    public int getTicketsSold() {
        return tickets.size();
    }

    public double getRevenue() {
        return getTicketsSold() * price;
    }

    public double getCapacityUtilization() {
        return capacity > 0 ? (double) getTicketsSold() / capacity * 100 : 0;
    }
}

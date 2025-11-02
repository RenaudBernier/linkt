package com.linkt.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "start_date_time", nullable = false)
    private String startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private String endDateTime;

    private String location;
    private String coordinates;
    private int capacity;

    @Column(name = "image_url")
    private String imageUrl;

    private double price;

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
}

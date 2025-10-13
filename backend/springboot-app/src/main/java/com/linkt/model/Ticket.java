package com.linkt.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Column(unique = true)
    private String qrCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    public Ticket() {}

    public Ticket(Student student, Event event) {
        this.student = student;
        this.event = event;
        this.generateQRCode();
    }
    public Long getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
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
    public String generateQRCode() {
        this.qrCode = UUID.randomUUID().toString();
        return this.qrCode;
    }  
    public boolean validateQRCode(String code) {
        return this.qrCode.equals(code);
    }

}
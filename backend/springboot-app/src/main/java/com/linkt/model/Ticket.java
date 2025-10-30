package com.linkt.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "is_scanned")
    private Boolean isScanned = false;

    @Column(name = "scanned_at")
    private String scannedAt;

    @ManyToOne
    @JoinColumn(name = "scanned_by")
    private User scannedBy;

    public Ticket() {}

    public Ticket(Student student, Event event) {
        this.student = student;
        this.event = event;
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
        if (this.ticketId == null || this.event == null) {
            return null; // Will be generated after save
        }

        // Generate simple format: LINKT-{eventId}-{ticketId}
        this.qrCode = String.format("LINKT-%d-%d", this.event.getEventId(), this.ticketId);
        return this.qrCode;
    }

    public boolean validateQRCode(String code) {
        return this.qrCode != null && this.qrCode.equals(code);
    }

    public Boolean getIsScanned() {
        return isScanned;
    }

    public void setIsScanned(Boolean isScanned) {
        this.isScanned = isScanned;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

    public User getScannedBy() {
        return scannedBy;
    }

    public void setScannedBy(User scannedBy) {
        this.scannedBy = scannedBy;
    }

    public void markAsScanned(User organizer) {
        this.isScanned = true;
        this.scannedAt = LocalDateTime.now().toString();
        this.scannedBy = organizer;
    }

}
package com.linkt.dto;

public class TicketData {
    private Long ticketId;
    private String studentName;
    private String studentEmail;
    private String eventName;
    private String eventDate;
    private String ticketType;

    public TicketData() {}

    public TicketData(Long ticketId, String studentName, String studentEmail, String eventName, String eventDate, String ticketType) {
        this.ticketId = ticketId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.ticketType = ticketType;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }
}

package com.linkt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long eventId;
    private String title;
    private String description;
    private String eventType;
    private String location;
    private String startDateTime;
    private String endDateTime;
    private int capacity;
    private double price;
    private int ticketCount;
    private int scannedTicketCount;
    private Long organizerId;
}

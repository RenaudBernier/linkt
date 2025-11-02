package com.linkt.dto;

import lombok.Data;

@Data
public class EventDTO {
    private String title;
    private String description;
    private String eventType;
    private double price;
    private String startDateTime;
    private String endDateTime;
    private String location;
    private int capacity;
    private String image;
}

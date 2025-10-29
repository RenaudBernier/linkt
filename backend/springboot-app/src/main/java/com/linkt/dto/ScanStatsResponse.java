package com.linkt.dto;

public class ScanStatsResponse {
    private Long eventId;
    private String eventName;
    private Integer totalTickets;
    private Integer scannedCount;
    private Integer remainingCount;

    public ScanStatsResponse() {}

    public ScanStatsResponse(Long eventId, String eventName, Integer totalTickets, Integer scannedCount, Integer remainingCount) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.totalTickets = totalTickets;
        this.scannedCount = scannedCount;
        this.remainingCount = remainingCount;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Integer totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Integer getScannedCount() {
        return scannedCount;
    }

    public void setScannedCount(Integer scannedCount) {
        this.scannedCount = scannedCount;
    }

    public Integer getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(Integer remainingCount) {
        this.remainingCount = remainingCount;
    }
}

package com.linkt.dto;

import java.util.List;

public class GlobalStatsResponse {
    private Long totalEvents;
    private Long totalTickets;
    private Long totalScannedTickets;
    private Long totalUnscannedTickets;
    private Long totalStudents;
    private Long totalOrganizers;
    private Double scanRate;
    private List<EventStats> topEvents;
    private List<ParticipationTrend> participationTrends;

    public GlobalStatsResponse() {}

    public GlobalStatsResponse(Long totalEvents, Long totalTickets, Long totalScannedTickets,
                              Long totalUnscannedTickets, Long totalStudents, Long totalOrganizers,
                              Double scanRate, List<EventStats> topEvents,
                              List<ParticipationTrend> participationTrends) {
        this.totalEvents = totalEvents;
        this.totalTickets = totalTickets;
        this.totalScannedTickets = totalScannedTickets;
        this.totalUnscannedTickets = totalUnscannedTickets;
        this.totalStudents = totalStudents;
        this.totalOrganizers = totalOrganizers;
        this.scanRate = scanRate;
        this.topEvents = topEvents;
        this.participationTrends = participationTrends;
    }

    // Getters and Setters
    public Long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(Long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public Long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Long getTotalScannedTickets() {
        return totalScannedTickets;
    }

    public void setTotalScannedTickets(Long totalScannedTickets) {
        this.totalScannedTickets = totalScannedTickets;
    }

    public Long getTotalUnscannedTickets() {
        return totalUnscannedTickets;
    }

    public void setTotalUnscannedTickets(Long totalUnscannedTickets) {
        this.totalUnscannedTickets = totalUnscannedTickets;
    }

    public Long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Long getTotalOrganizers() {
        return totalOrganizers;
    }

    public void setTotalOrganizers(Long totalOrganizers) {
        this.totalOrganizers = totalOrganizers;
    }

    public Double getScanRate() {
        return scanRate;
    }

    public void setScanRate(Double scanRate) {
        this.scanRate = scanRate;
    }

    public List<EventStats> getTopEvents() {
        return topEvents;
    }

    public void setTopEvents(List<EventStats> topEvents) {
        this.topEvents = topEvents;
    }

    public List<ParticipationTrend> getParticipationTrends() {
        return participationTrends;
    }

    public void setParticipationTrends(List<ParticipationTrend> participationTrends) {
        this.participationTrends = participationTrends;
    }

    // Inner class for event statistics
    public static class EventStats {
        private Long eventId;
        private String eventName;
        private Long ticketCount;
        private Long scannedCount;

        public EventStats() {}

        public EventStats(Long eventId, String eventName, Long ticketCount, Long scannedCount) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.ticketCount = ticketCount;
            this.scannedCount = scannedCount;
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

        public Long getTicketCount() {
            return ticketCount;
        }

        public void setTicketCount(Long ticketCount) {
            this.ticketCount = ticketCount;
        }

        public Long getScannedCount() {
            return scannedCount;
        }

        public void setScannedCount(Long scannedCount) {
            this.scannedCount = scannedCount;
        }
    }

    // Inner class for participation trends
    public static class ParticipationTrend {
        private String date;
        private Long ticketsIssued;
        private Long ticketsScanned;

        public ParticipationTrend() {}

        public ParticipationTrend(String date, Long ticketsIssued, Long ticketsScanned) {
            this.date = date;
            this.ticketsIssued = ticketsIssued;
            this.ticketsScanned = ticketsScanned;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Long getTicketsIssued() {
            return ticketsIssued;
        }

        public void setTicketsIssued(Long ticketsIssued) {
            this.ticketsIssued = ticketsIssued;
        }

        public Long getTicketsScanned() {
            return ticketsScanned;
        }

        public void setTicketsScanned(Long ticketsScanned) {
            this.ticketsScanned = ticketsScanned;
        }
    }
}

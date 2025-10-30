package com.linkt.dto;

public class ScanResponse {
    private boolean valid;
    private String message;
    private String status; // SUCCESS, ALREADY_SCANNED, INVALID, WRONG_EVENT, EXPIRED
    private TicketData ticketData;
    private String scannedAt;
    private String scannedBy;

    public ScanResponse() {}

    public ScanResponse(boolean valid, String message, String status) {
        this.valid = valid;
        this.message = message;
        this.status = status;
    }

    public ScanResponse(boolean valid, String message, String status, TicketData ticketData, String scannedAt, String scannedBy) {
        this.valid = valid;
        this.message = message;
        this.status = status;
        this.ticketData = ticketData;
        this.scannedAt = scannedAt;
        this.scannedBy = scannedBy;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TicketData getTicketData() {
        return ticketData;
    }

    public void setTicketData(TicketData ticketData) {
        this.ticketData = ticketData;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getScannedBy() {
        return scannedBy;
    }

    public void setScannedBy(String scannedBy) {
        this.scannedBy = scannedBy;
    }
}

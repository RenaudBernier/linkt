package com.linkt.dto;

public class ScanRequest {
    private String qrCode;
    private Long eventId;

    public ScanRequest() {}

    public ScanRequest(String qrCode, Long eventId) {
        this.qrCode = qrCode;
        this.eventId = eventId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}

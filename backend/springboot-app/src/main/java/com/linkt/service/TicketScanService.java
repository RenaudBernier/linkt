package com.linkt.service;

import com.linkt.dto.ScanResponse;
import com.linkt.dto.ScanStatsResponse;
import com.linkt.dto.TicketData;
import com.linkt.model.Event;
import com.linkt.model.Ticket;
import com.linkt.model.User;
import com.linkt.repository.EventRepository;
import com.linkt.repository.TicketRepository;
import com.linkt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class TicketScanService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ScanResponse validateTicket(String qrCode, Long eventId, Long organizerId) {
        // 1. Find ticket by QR code
        Optional<Ticket> ticketOpt = ticketRepository.findByQrCode(qrCode);
        if (!ticketOpt.isPresent()) {
            return new ScanResponse(false, "Invalid ticket code", "INVALID");
        }

        Ticket ticket = ticketOpt.get();

        // 2. Verify ticket belongs to specified event
        if (!ticket.getEvent().getEventId().equals(eventId)) {
            String actualEventName = ticket.getEvent().getTitle();
            return new ScanResponse(
                false,
                "Ticket is for a different event: " + actualEventName,
                "WRONG_EVENT"
            );
        }

        // 3. Check if already scanned
        if (ticket.getIsScanned() != null && ticket.getIsScanned()) {
            String scannedBy = ticket.getScannedBy() != null
                ? ticket.getScannedBy().getFirstName() + " " + ticket.getScannedBy().getLastName()
                : "Unknown";

            return new ScanResponse(
                false,
                "Ticket already scanned at " + formatDateTime(ticket.getScannedAt()) + " by " + scannedBy,
                "ALREADY_SCANNED",
                null,
                ticket.getScannedAt(),
                scannedBy
            );
        }

        // 4. Verify organizer has permission for this event
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizer().getUserId().equals(organizerId)) {
            throw new RuntimeException("Unauthorized: You are not the organizer of this event");
        }

        // 5. Mark ticket as scanned
        User organizer = userRepository.findById(organizerId)
            .orElseThrow(() -> new RuntimeException("Organizer not found"));

        ticket.markAsScanned(organizer);
        ticketRepository.save(ticket);

        // 6. Build ticket data
        TicketData ticketData = new TicketData(
            ticket.getTicketId(),
            ticket.getStudent().getFirstName() + " " + ticket.getStudent().getLastName(),
            ticket.getStudent().getEmail(),
            event.getTitle(),
            event.getStartDateTime(),
            "General Admission"
        );

        // 7. Return success response
        return new ScanResponse(
            true,
            "Ticket successfully scanned for " + ticketData.getStudentName(),
            "SUCCESS",
            ticketData,
            ticket.getScannedAt(),
            organizer.getFirstName() + " " + organizer.getLastName()
        );
    }

    public ScanStatsResponse getScanStats(Long eventId, Long organizerId) {
        // 1. Verify organizer permission
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getOrganizer().getUserId().equals(organizerId)) {
            throw new RuntimeException("Unauthorized: You are not the organizer of this event");
        }

        // 2. Get total tickets for event
        Long totalTickets = (long) ticketRepository.findByEvent_EventId(eventId).size();

        // 3. Get scanned count
        Long scannedCount = ticketRepository.countByEvent_EventIdAndIsScanned(eventId, true);

        // 4. Calculate remaining
        Integer remainingCount = (int) (totalTickets - scannedCount);

        // 5. Return stats
        return new ScanStatsResponse(
            eventId,
            event.getTitle(),
            totalTickets.intValue(),
            scannedCount.intValue(),
            remainingCount
        );
    }

    private String formatDateTime(String dateTime) {
        if (dateTime == null) return "unknown time";
        try {
            LocalDateTime ldt = LocalDateTime.parse(dateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            return ldt.format(formatter);
        } catch (Exception e) {
            return dateTime;
        }
    }
}

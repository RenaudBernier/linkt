package com.linkt.service;

import com.linkt.dto.StudentRegistrationDTO;
import com.linkt.model.Event;
import com.linkt.model.Organizer;
import com.linkt.model.Ticket;
import com.linkt.repository.EventRepository;
import com.linkt.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Get all registered students for a specific event
     * Verifies that the organizer owns the event before returning students
     */
    public List<StudentRegistrationDTO> getRegisteredStudentsForEvent(Long eventId, Organizer organizer) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verify organizer owns this event
        if (!event.getOrganizer().getUserId().equals(organizer.getUserId())) {
            throw new RuntimeException("You don't have permission to view this event's students");
        }

        // Get all tickets for this event
        List<Ticket> tickets = ticketRepository.findByEvent_EventId(eventId);

        // Convert tickets to StudentRegistrationDTOs
        return tickets.stream()
                .map(this::convertTicketToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Ticket to StudentRegistrationDTO
     */
    private StudentRegistrationDTO convertTicketToDTO(Ticket ticket) {
        return new StudentRegistrationDTO(
                ticket.getStudent().getUserId(),
                ticket.getStudent().getFirstName(),
                ticket.getStudent().getLastName(),
                ticket.getStudent().getEmail(),
                ticket.getStudent().getPhoneNumber(),
                ticket.getTicketId(),
                ticket.getQrCode(),
                ticket.getIsScanned(),
                ticket.getScannedAt()
        );
    }
}

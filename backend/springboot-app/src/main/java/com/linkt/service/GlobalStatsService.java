package com.linkt.service;

import com.linkt.dto.GlobalStatsResponse;
import com.linkt.dto.GlobalStatsResponse.EventStats;
import com.linkt.dto.GlobalStatsResponse.ParticipationTrend;
import com.linkt.model.Event;
import com.linkt.model.Ticket;
import com.linkt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GlobalStatsService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    public GlobalStatsResponse getGlobalStatistics() {
        // Get basic counts
        Long totalEvents = eventRepository.count();
        Long totalTickets = ticketRepository.count();
        Long totalStudents = studentRepository.count();
        Long totalOrganizers = organizerRepository.count();

        // Get all tickets to calculate scanned/unscanned
        List<Ticket> allTickets = ticketRepository.findAll();
        Long totalScannedTickets = allTickets.stream()
                .filter(ticket -> ticket.getIsScanned() != null && ticket.getIsScanned())
                .count();
        Long totalUnscannedTickets = totalTickets - totalScannedTickets;

        // Calculate scan rate
        Double scanRate = totalTickets > 0
                ? (totalScannedTickets.doubleValue() / totalTickets.doubleValue()) * 100
                : 0.0;

        // Get top events by ticket count
        List<EventStats> topEvents = getTopEventsByTicketCount();

        // Get participation trends
        List<ParticipationTrend> participationTrends = getParticipationTrends(allTickets);

        return new GlobalStatsResponse(
                totalEvents,
                totalTickets,
                totalScannedTickets,
                totalUnscannedTickets,
                totalStudents,
                totalOrganizers,
                scanRate,
                topEvents,
                participationTrends
        );
    }

    private List<EventStats> getTopEventsByTicketCount() {
        List<Event> allEvents = eventRepository.findAll();

        return allEvents.stream()
                .map(event -> {
                    List<Ticket> eventTickets = ticketRepository.findByEvent_EventId(event.getEventId());
                    Long ticketCount = (long) eventTickets.size();
                    Long scannedCount = eventTickets.stream()
                            .filter(ticket -> ticket.getIsScanned() != null && ticket.getIsScanned())
                            .count();

                    return new EventStats(
                            event.getEventId(),
                            event.getTitle(),
                            ticketCount,
                            scannedCount
                    );
                })
                .sorted((e1, e2) -> Long.compare(e2.getTicketCount(), e1.getTicketCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<ParticipationTrend> getParticipationTrends(List<Ticket> allTickets) {
        // Group tickets by the date their event starts
        Map<LocalDate, List<Ticket>> ticketsByDate = new HashMap<>();

        for (Ticket ticket : allTickets) {
            if (ticket.getEvent() != null && ticket.getEvent().getStartDateTime() != null) {
                try {
                    // Parse the string datetime to LocalDateTime, then get the date
                    LocalDate eventDate = LocalDate.parse(ticket.getEvent().getStartDateTime().substring(0, 10));
                    ticketsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(ticket);
                } catch (Exception e) {
                    // Skip tickets with invalid date formats
                    continue;
                }
            }
        }

        // Convert to ParticipationTrend objects
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return ticketsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Ticket> tickets = entry.getValue();
                    Long ticketsIssued = (long) tickets.size();
                    Long ticketsScanned = tickets.stream()
                            .filter(ticket -> ticket.getIsScanned() != null && ticket.getIsScanned())
                            .count();

                    return new ParticipationTrend(
                            date.format(formatter),
                            ticketsIssued,
                            ticketsScanned
                    );
                })
                .sorted(Comparator.comparing(ParticipationTrend::getDate))
                .collect(Collectors.toList());
    }
}

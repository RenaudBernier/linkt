package com.linkt.repository;

import com.linkt.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStudent_UserId(Long userId);
    Optional<Ticket> findByQrCode(String qrCode);
    List<Ticket> findByEvent_EventId(Long eventId);
    Long countByEvent_EventIdAndIsScanned(Long eventId, Boolean isScanned);
    List<Ticket> findByEvent_EventIdAndIsScanned(Long eventId, Boolean isScanned);
}

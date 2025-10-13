package com.linkt.repository;

import com.linkt.model.SavedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedEventRepository extends JpaRepository<SavedEvent, Long> {
    List<SavedEvent> findByStudent_UserId(Long userId);
    Optional<SavedEvent> findByStudent_UserIdAndEvent_EventId(Long userId, Long eventId);
    void deleteByStudent_UserIdAndEvent_EventId(Long userId, Long eventId);
}

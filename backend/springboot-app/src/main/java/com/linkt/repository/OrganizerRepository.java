package com.linkt.repository;

import com.linkt.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    List<Organizer> findByIsApproved(boolean isApproved);
}

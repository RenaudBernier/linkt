package com.linkt.service;

import com.linkt.model.Organizer;
import com.linkt.repository.OrganizerRepository;
import com.linkt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Organizer> getPendingOrganizers() {
        return organizerRepository.findByIsApproved(false);
    }

    public void approveOrganizer(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user instanceof Organizer) {
                ((Organizer) user).setApproved(true);
                userRepository.save(user);
            }
        });
    }
}

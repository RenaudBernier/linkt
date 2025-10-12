package com.linkt.service;

import com.linkt.dto.AdministratorDTO;
import com.linkt.model.Administrator;
import com.linkt.model.User;
import com.linkt.repository.AdministratorRepository;
import com.linkt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdministratorService {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private UserRepository userRepository;

    public AdministratorDTO createAdministrator(AdministratorDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        if (administratorRepository.findByUserId(dto.getUserId()).isPresent()) {
            throw new RuntimeException("User is already an administrator");
        }

        Administrator admin = new Administrator(user);
        Administrator savedAdmin = administratorRepository.save(admin);
        return convertToDTO(savedAdmin);
    }
    public List<AdministratorDTO> getAllAdministrators() {
        return administratorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public AdministratorDTO getAdministratorById(Long id) {
        Administrator admin = administratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrator not found with id: " + id));
        return convertToDTO(admin);
    }
    public AdministratorDTO getAdministratorByUserId(Long userId) {
        Administrator admin = administratorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Administrator not found for user id: " + userId));
        return convertToDTO(admin);
    }
    public boolean isAdministrator(Long userId) {
        return administratorRepository.existsByUserId(userId);
    }
    public void deleteAdministrator(Long id) {
        Administrator admin = administratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrator not found with id: " + id));
        administratorRepository.delete(admin);
    }
    private AdministratorDTO convertToDTO(Administrator admin) {
        User user = admin.getUser();
        return new AdministratorDTO(
                admin.getId(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
        );
    }
}
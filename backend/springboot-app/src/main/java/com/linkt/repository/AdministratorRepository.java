package com.linkt.repository;

import com.linkt.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administator, Long> {
    Optional<Administator> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
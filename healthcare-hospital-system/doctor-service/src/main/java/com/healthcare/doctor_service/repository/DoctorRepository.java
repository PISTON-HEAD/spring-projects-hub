package com.healthcare.doctor_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcare.doctor_service.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
 boolean existsByEmail(String email);
 Page<Doctor> findAll(Pageable pageable);
 Optional<Doctor> findByEmail(String email);
}

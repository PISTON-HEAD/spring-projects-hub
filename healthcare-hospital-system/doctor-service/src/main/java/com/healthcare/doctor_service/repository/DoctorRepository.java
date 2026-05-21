package com.healthcare.doctor_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcare.doctor_service.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
 boolean existsByEmail(String email);

 Optional<Doctor> findByEmail(String email);
}

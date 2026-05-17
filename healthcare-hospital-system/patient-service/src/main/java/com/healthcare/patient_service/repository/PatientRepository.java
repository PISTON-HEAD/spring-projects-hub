package com.healthcare.patient_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcare.patient_service.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

 boolean existsByEmail(String email);

 Optional<Patient> findByEmail(String email);
}

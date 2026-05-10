package com.fhir.api.repository;

import com.fhir.api.entity.PatientIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientIdentifierRepository extends JpaRepository<PatientIdentifier, UUID> {
}

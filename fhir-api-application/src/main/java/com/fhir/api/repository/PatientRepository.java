package com.fhir.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fhir.api.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers WHERE p.gender = :gender")
    List<Patient> findByGender(@Param("gender") String gender);
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers WHERE LOWER(p.lastName) = LOWER(:lastName)")
    List<Patient> findByLastName(@Param("lastName") String lastName);
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers WHERE LOWER(p.firstName) = LOWER(:firstName)")
    List<Patient> findByFirstName(@Param("firstName") String firstName);
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers WHERE p.id = :id")
    Optional<Patient> findByIdWithIdentifiers(@Param("id") UUID id);
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers i WHERE i.identifierValue = :identifier")
    Optional<Patient> findByIdentifier(@Param("identifier") String identifier);
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers")
    List<Patient> findAllWithIdentifiers();
    
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.identifiers WHERE " +
           "(:firstName IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "(:dateOfBirth IS NULL OR p.dateOfBirth = :dateOfBirth)")
    List<Patient> searchPatients(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("gender") String gender,
        @Param("dateOfBirth") LocalDate dateOfBirth
    );
}

package com.fhir.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fhir.api.entity.Patient;
import com.fhir.api.entity.PatientIdentifier;
import com.fhir.api.repository.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final FhirConversionService fhirConversionService;

    public PatientService(PatientRepository patientRepository, FhirConversionService fhirConversionService) {
        this.patientRepository = patientRepository;
        this.fhirConversionService = fhirConversionService;
    }

    @Transactional(readOnly = true)
    public Optional<Patient> findByIdentifier(String identifier) {
        return patientRepository.findByIdentifier(identifier);
    }

    @Transactional(readOnly = true)
    public List<Patient> findByGender(String gender) {
        return patientRepository.findByGender(gender);
    }

    @Transactional(readOnly = true)
    public List<Patient> findByLastName(String lastName) {
        return patientRepository.findByLastName(lastName);
    }

    @Transactional(readOnly = true)
    public List<Patient> findByFirstName(String firstName) {
        return patientRepository.findByFirstName(firstName);
    }

    @Transactional(readOnly = true)
    public List<Patient> searchPatients(String firstName, String lastName, String gender, LocalDate dateOfBirth) {
        // If identifier search needed, handle separately
        // This method supports combined search of multiple fields
        return patientRepository.searchPatients(firstName, lastName, gender, dateOfBirth);
    }

    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAllWithIdentifiers();
    }

    @Transactional
    public Patient savePatient(Patient patient) {
        // Set patient reference in identifiers
        if (patient.getIdentifiers() != null) {
            for (PatientIdentifier identifier : patient.getIdentifiers()) {
                identifier.setPatient(patient);
            }
        }
        return patientRepository.save(patient);
    }

    public String convertToFhirJson(Patient patient) {
        org.hl7.fhir.r4.model.Patient fhirPatient = fhirConversionService.convertToFhirPatient(patient);
        return fhirConversionService.toJson(fhirPatient);
    }

    public String convertToFhirBundleJson(List<Patient> patients) {
        org.hl7.fhir.r4.model.Bundle bundle = fhirConversionService.createBundle(patients);
        return fhirConversionService.toJson(bundle);
    }
}

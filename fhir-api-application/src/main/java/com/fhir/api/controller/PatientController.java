package com.fhir.api.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fhir.api.dto.CreatePatientRequest;
import com.fhir.api.dto.PatientBundleResultDto;
import com.fhir.api.dto.PatientIdentifierDto;
import com.fhir.api.dto.PatientResponse;
import com.fhir.api.dto.PatientResultDto;
import com.fhir.api.entity.Patient;
import com.fhir.api.entity.PatientIdentifier;
import com.fhir.api.repository.PatientRepository;
import com.fhir.api.service.PatientService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;
    private final PatientService patientService;

    public PatientController(PatientRepository patientRepository, PatientService patientService) {
        this.patientRepository = patientRepository;
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PatientBundleResultDto> searchPatients(
            @RequestParam(required = false) String fname,
            @RequestParam(required = false) String lname,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String dateOfBirth
    ) {
        try {
            List<Patient> patients;
            
            // Map fname/lname to firstName/lastName if provided
            String searchFirstName = firstName != null ? firstName : fname;
            String searchLastName = lastName != null ? lastName : lname;
            String searchBirthDate = dateOfBirth != null ? dateOfBirth : birthDate;
            
            // If identifier is provided, search by identifier first
            if (identifier != null && !identifier.isEmpty()) {
                Optional<Patient> patient = patientService.findByIdentifier(identifier);
                patients = patient.map(List::of).orElse(List.of());
            }
            // If any search criteria is provided, use dynamic search
            else if (searchFirstName != null || searchLastName != null || gender != null || searchBirthDate != null) {
                LocalDate parsedDate = null;
                if (searchBirthDate != null && !searchBirthDate.isEmpty()) {
                    try {
                        parsedDate = LocalDate.parse(searchBirthDate);
                    } catch (Exception e) {
                        // Invalid date format, ignore
                    }
                }
                patients = patientService.searchPatients(searchFirstName, searchLastName, gender, parsedDate);
            }
            // No criteria, return all patients
            else {
                patients = patientService.findAll();
            }
            
            // Convert to DTO
            List<PatientResultDto> patientDtos = patients.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            String fhirBundle = patientService.convertToFhirBundleJson(patients);
            
            PatientBundleResultDto response = PatientBundleResultDto.builder()
                    .total(patients.size())
                    .fhirBundle(fhirBundle)
                    .patients(patientDtos)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PatientBundleResultDto errorResponse = PatientBundleResultDto.builder()
                    .total(0)
                    .fhirBundle("{\"error\": \"" + e.getMessage() + "\"}")
                    .patients(List.of())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PatientResponse> createPatient(@RequestBody CreatePatientRequest request) {
        try {
            Patient patient = new Patient();
            patient.setFirstName(request.getFirstName());
            patient.setMiddleName(request.getMiddleName());
            patient.setLastName(request.getLastName());
            patient.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            patient.setGender(request.getGender());
            patient.setPhone(request.getPhone());
            patient.setEmail(request.getEmail());
            patient.setAddressLine1(request.getAddressLine1());
            patient.setAddressLine2(request.getAddressLine2());
            patient.setCity(request.getCity());
            patient.setState(request.getState());
            patient.setPostalCode(request.getPostalCode());
            patient.setCountry(request.getCountry());
            patient.setActive(true);

            // Create identifiers
            if (request.getIdentifiers() != null) {
                List<PatientIdentifier> identifiers = new ArrayList<>();
                for (PatientIdentifierDto dto : request.getIdentifiers()) {
                    PatientIdentifier identifier = new PatientIdentifier();
                    identifier.setIdentifierType(dto.getType());
                    identifier.setIdentifierValue(dto.getValue());
                    identifier.setSystem(dto.getSystem());
                    identifier.setPatient(patient);
                    identifiers.add(identifier);
                }
                patient.setIdentifiers(identifiers);
            }

            Patient savedPatient = patientRepository.save(patient);

            PatientResponse response = PatientResponse.builder()
                    .id(savedPatient.getId())
                    .firstName(savedPatient.getFirstName())
                    .lastName(savedPatient.getLastName())
                    .message("Patient created successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            PatientResponse errorResponse = PatientResponse.builder()
                    .message("Error creating patient: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    private PatientResultDto convertToDto(Patient patient) {
        List<PatientIdentifierDto> identifierDtos = patient.getIdentifiers() != null 
                ? patient.getIdentifiers().stream()
                    .map(id -> new PatientIdentifierDto(id.getIdentifierType(), id.getIdentifierValue(), id.getSystem()))
                    .collect(Collectors.toList())
                : List.of();
        
        return PatientResultDto.builder()
                .id(patient.getId().toString())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null)
                .identifiers(identifierDtos)
                .fhirResource(patientService.convertToFhirJson(patient))
                .build();
    }
}

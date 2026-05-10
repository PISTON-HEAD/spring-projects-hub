package com.fhir.api.graphql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.fhir.api.dto.PatientBundleResultDto;
import com.fhir.api.dto.PatientIdentifierDto;
import com.fhir.api.dto.PatientResultDto;
import com.fhir.api.entity.Patient;
import com.fhir.api.entity.PatientIdentifier;
import com.fhir.api.service.PatientService;

@Controller
public class PatientResolver {

    private final PatientService patientService;

    public PatientResolver(PatientService patientService) {
        this.patientService = patientService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public PatientResultDto patient(@Argument String identifier) {
        Optional<Patient> patientOpt = patientService.findByIdentifier(identifier);
        
        if (patientOpt.isEmpty()) {
            return PatientResultDto.builder()
                    .fhirResource("{}")
                    .build();
        }

        Patient patient = patientOpt.get();
        String fhirJson = patientService.convertToFhirJson(patient);

        return PatientResultDto.builder()
                .fhirResource(fhirJson)
                .id(patient.getId().toString())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null)
                .identifiers(convertIdentifiers(patient.getIdentifiers()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public PatientBundleResultDto patientsByGender(@Argument String gender) {
        List<Patient> patients = patientService.findByGender(gender);
        
        if (patients.isEmpty()) {
            return PatientBundleResultDto.builder()
                    .total(0)
                    .fhirBundle("{\"resourceType\":\"Bundle\",\"type\":\"searchset\",\"total\":0,\"entry\":[]}")
                    .patients(List.of())
                    .build();
        }

        String bundleJson = patientService.convertToFhirBundleJson(patients);

        return PatientBundleResultDto.builder()
                .total(patients.size())
                .fhirBundle(bundleJson)
                .patients(convertPatientsToDto(patients))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public PatientBundleResultDto patientsByLastName(@Argument String lastName) {
        List<Patient> patients = patientService.findByLastName(lastName);
        
        if (patients.isEmpty()) {
            return PatientBundleResultDto.builder()
                    .total(0)
                    .fhirBundle("{\"resourceType\":\"Bundle\",\"type\":\"searchset\",\"total\":0,\"entry\":[]}")
                    .patients(List.of())
                    .build();
        }

        String bundleJson = patientService.convertToFhirBundleJson(patients);

        return PatientBundleResultDto.builder()
                .total(patients.size())
                .fhirBundle(bundleJson)
                .patients(convertPatientsToDto(patients))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public PatientBundleResultDto patientsByFirstName(@Argument String firstName) {
        List<Patient> patients = patientService.findByFirstName(firstName);
        
        if (patients.isEmpty()) {
            return PatientBundleResultDto.builder()
                    .total(0)
                    .fhirBundle("{\"resourceType\":\"Bundle\",\"type\":\"searchset\",\"total\":0,\"entry\":[]}")
                    .patients(List.of())
                    .build();
        }

        String bundleJson = patientService.convertToFhirBundleJson(patients);

        return PatientBundleResultDto.builder()
                .total(patients.size())
                .fhirBundle(bundleJson)
                .patients(convertPatientsToDto(patients))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public PatientBundleResultDto allPatients() {
        List<Patient> patients = patientService.findAll();
        
        if (patients.isEmpty()) {
            return PatientBundleResultDto.builder()
                    .total(0)
                    .fhirBundle("{\"resourceType\":\"Bundle\",\"type\":\"searchset\",\"total\":0,\"entry\":[]}")
                    .patients(List.of())
                    .build();
        }

        String bundleJson = patientService.convertToFhirBundleJson(patients);

        return PatientBundleResultDto.builder()
                .total(patients.size())
                .fhirBundle(bundleJson)
                .patients(convertPatientsToDto(patients))
                .build();
    }

    private List<PatientResultDto> convertPatientsToDto(List<Patient> patients) {
        return patients.stream()
                .map(patient -> PatientResultDto.builder()
                        .id(patient.getId().toString())
                        .firstName(patient.getFirstName())
                        .lastName(patient.getLastName())
                        .gender(patient.getGender())
                        .dateOfBirth(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null)
                        .identifiers(convertIdentifiers(patient.getIdentifiers()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<PatientIdentifierDto> convertIdentifiers(List<PatientIdentifier> identifiers) {
        if (identifiers == null) {
            return List.of();
        }
        return identifiers.stream()
                .map(id -> new PatientIdentifierDto(
                        id.getIdentifierType(),
                        id.getIdentifierValue(),
                        id.getSystem()
                ))
                .collect(Collectors.toList());
    }
}

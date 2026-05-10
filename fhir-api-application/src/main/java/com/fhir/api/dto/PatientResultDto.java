package com.fhir.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResultDto {
    private String fhirResource;
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private String dateOfBirth;
    private List<PatientIdentifierDto> identifiers;
}

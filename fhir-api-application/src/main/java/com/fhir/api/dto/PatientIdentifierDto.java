package com.fhir.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientIdentifierDto {
    private String type;
    private String value;
    private String system;
}

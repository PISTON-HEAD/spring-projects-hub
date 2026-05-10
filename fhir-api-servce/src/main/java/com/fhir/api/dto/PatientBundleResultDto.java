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
public class PatientBundleResultDto {
    private String fhirBundle;
    private Integer total;
    private List<PatientResultDto> patients;
}

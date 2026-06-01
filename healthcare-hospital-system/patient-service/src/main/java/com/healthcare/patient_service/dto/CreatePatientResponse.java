package com.healthcare.patient_service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePatientResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        LocalDateTime createdAt) implements Serializable {

}

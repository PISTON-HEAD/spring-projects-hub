package com.healthcare.appointment_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.healthcare.appointment_service.enums.AppointmentStatus;
import java.io.Serializable;

public record AppointmentResponse(
    UUID id,
    UUID patientId,
    UUID doctorId,
    UUID slotId,
    AppointmentStatus status,
    String reason,
    String notes,
    LocalDateTime appointmentDateTime,
    LocalDateTime createdAt
) implements Serializable {
    
}

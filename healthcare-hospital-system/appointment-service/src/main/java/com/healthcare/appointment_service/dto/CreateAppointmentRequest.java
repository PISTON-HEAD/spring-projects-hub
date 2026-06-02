package com.healthcare.appointment_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
    UUID patientId,
    UUID doctorId,
    UUID slotId,
    String reason,
    LocalDateTime appointmentTime
) {
} 
package com.healthcare.doctor_service.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReserveSlotRequest(
  @NotNull(message = "Appointment id is required") UUID appointmentId) {

}

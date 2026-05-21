package com.healthcare.doctor_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateDoctorSlotRequest(
  @NotNull(message = "Doctor id is required") UUID doctorId,

  @NotNull(message = "Start time is required") LocalDateTime startTime,

  @NotNull(message = "End time is required") LocalDateTime endTime) {

}

package com.healthcare.doctor_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorResponse(
  UUID id,
  String firstName,
  String lastName,
  String specialization,
  String email,
  String phoneNumber,
  Boolean active,
  LocalDateTime createdAt,
  LocalDateTime updatedAt) {

}

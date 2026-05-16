package com.healthcare.hospital_booking_system.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatePatientResponse(
  UUID id,
  String firstName,
  String lastName,

  String email,
  String phoneNumber,
  LocalDateTime createdAt) {

}

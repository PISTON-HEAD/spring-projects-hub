package com.healthcare.doctor_service.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateDoctorRequest(
  @NotBlank(message = "First name is required") String firstName,

  String lastName,

  @NotBlank(message = "Specialization is required") String specialization,

  @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email,

  String phoneNumber) {

}

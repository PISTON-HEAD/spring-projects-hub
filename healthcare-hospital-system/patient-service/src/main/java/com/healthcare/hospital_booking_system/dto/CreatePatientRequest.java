package com.healthcare.hospital_booking_system.dto;

import com.healthcare.hospital_booking_system.entity.PatientAddress;
import com.healthcare.hospital_booking_system.enums.PatientGender;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePatientRequest(
    @NotBlank String firstName,

    String lastName,

    @NotNull Integer age,

    @NotBlank PatientGender gender,

    @Email @NotBlank String email,

    String phoneNumber,

    @Valid @NotNull PatientAddress address) {

}

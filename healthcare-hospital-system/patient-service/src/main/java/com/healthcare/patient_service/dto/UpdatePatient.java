package com.healthcare.patient_service.dto;

import com.healthcare.patient_service.entity.PatientAddress;
import com.healthcare.patient_service.enums.PatientGender;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

public record UpdatePatient(

  String firstName,

  String lastName,

  Integer age,

  PatientGender gender,

  @Email String email,

  String phoneNumber,

  @Valid PatientAddress address) {
}
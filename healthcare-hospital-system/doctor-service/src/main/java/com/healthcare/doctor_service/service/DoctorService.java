package com.healthcare.doctor_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import com.healthcare.doctor_service.dto.CreateDoctorRequest;
import com.healthcare.doctor_service.dto.DoctorResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.repository.DoctorRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class DoctorService {

 private final DoctorRepository repository;

 @Transactional
 public DoctorResponse createDoctor(CreateDoctorRequest request) {
  if (repository.existsByEmail(request.email())) {
   throw new IllegalArgumentException("Doctor with this email already exists");
  }

  Doctor doctor = Doctor.builder()
    .firstName(request.firstName())
    .lastName(request.lastName())
    .specialization(request.specialization())
    .email(request.email())
    .phoneNumber(request.phoneNumber())
    .active(true)
    .build();

  Doctor savedDoctor = repository.save(doctor);

  return tDoctorResponse(savedDoctor);
 }

 @Transactional
 public DoctorResponse getDoctorById(UUID doctorId) {
  Doctor doctor = repository.findById(doctorId)
    .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

  return tDoctorResponse(doctor);
 }

 private DoctorResponse tDoctorResponse(Doctor savedDoctor) {
  DoctorResponse response = new DoctorResponse(savedDoctor.getId(), savedDoctor.getFirstName(),
    savedDoctor.getLastName(), savedDoctor.getSpecialization(), savedDoctor.getEmail(), savedDoctor.getPhoneNumber(),
    savedDoctor.getActive(), savedDoctor.getCreatedAt());

  return response;

 }
}

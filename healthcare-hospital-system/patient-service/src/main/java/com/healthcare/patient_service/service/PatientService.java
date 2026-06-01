package com.healthcare.patient_service.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.healthcare.patient_service.dto.CreatePatientRequest;
import com.healthcare.patient_service.dto.CreatePatientResponse;
import com.healthcare.patient_service.dto.UpdatePatient;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.entity.PatientAddress;
import com.healthcare.patient_service.exception.PatientNotFoundException;
import com.healthcare.patient_service.repository.PatientRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PatientService {

  private final PatientRepository patientRepository;

  @Transactional
  public Object createPatient(CreatePatientRequest request) {
    if (patientRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists");
    }

    Patient savePatient = Patient.builder().address(request.address()).age(request.age()).email(request.email())
        .firstName(request.firstName()).lastName(request.lastName()).phoneNumber(request.phoneNumber())
        .gender(request.gender()).build();

    Patient savedPatient = patientRepository.save(savePatient);
    CreatePatientResponse patientResponse = new CreatePatientResponse(savedPatient.getId(),
        savedPatient.getFirstName(),
        savedPatient.getLastName(), savedPatient.getEmail(), savedPatient.getCreatedAt());
    return patientResponse;
  }

  @Transactional
  public CreatePatientResponse getPatientById(UUID patientId) {
    Patient savedPatient = patientRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + patientId));
    CreatePatientResponse patientResponse = new CreatePatientResponse(savedPatient.getId(), savedPatient.getFirstName(),
        savedPatient.getLastName(), savedPatient.getEmail(), savedPatient.getCreatedAt());
    return patientResponse;
  }

  @Transactional
  public Page<CreatePatientResponse> getAllPatients(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
    return patientRepository.findAll(pageable)
        .map(p -> new CreatePatientResponse(p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(), p.getCreatedAt()));
  }

  @Transactional
  public boolean existsById(UUID patientId) {
    return patientRepository.existsById(patientId);
  }

  @Transactional
  public CreatePatientResponse updatePatient(UUID patientId, UpdatePatient request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + patientId));

    if (request.firstName() != null) {
      patient.setFirstName(request.firstName());
    }

    if (request.lastName() != null) {
      patient.setLastName(request.lastName());
    }

    if (request.age() != null) {
      patient.setAge(request.age());
    }

    if (request.gender() != null) {
      patient.setGender(request.gender());
    }

    if (request.phoneNumber() != null) {
      patient.setPhoneNumber(request.phoneNumber());
    }

    if (request.address() != null) {
      PatientAddress address = patient.getAddress();
      address.setCountry(request.address().getCountry());
      address.setState(request.address().getState());
      address.setHouseName(request.address().getHouseName());
    }

    Patient savedPatient = patientRepository.saveAndFlush(patient);
    CreatePatientResponse patientResponse = new CreatePatientResponse(savedPatient.getId(), savedPatient.getFirstName(),
        savedPatient.getLastName(), savedPatient.getEmail(), savedPatient.getCreatedAt());
    return patientResponse;
  }
}

package com.healthcare.patient_service.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.patient_service.dto.CreatePatientRequest;
import com.healthcare.patient_service.dto.UpdatePatient;
import com.healthcare.patient_service.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
 private final PatientService service;

 @PostMapping
 @ResponseStatus(HttpStatus.CREATED)
 public ResponseEntity<?> createPatient(@Valid @RequestBody CreatePatientRequest patientRequest) {

  return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(service.createPatient(patientRequest));
 }

 @GetMapping
 public ResponseEntity<?> getAllPatients(
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size) {
  return ResponseEntity.ok(service.getAllPatients(page, size));
 }

 @GetMapping("/{patientId}")
 public ResponseEntity<?> getPatientById(@PathVariable UUID patientId) {
  return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getPatientById(patientId));
 }

 @GetMapping("/{patientId}/exists")
 public Map<String, Boolean> existsById(@PathVariable UUID patientId) {
  return Map.of("exists", service.existsById(patientId));
 }

 @PutMapping("/{patientId}")
 public ResponseEntity<?> updatePatient(@PathVariable UUID patientId, @Valid @RequestBody UpdatePatient request) {
  return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(service.updatePatient(patientId, request));
 }
}

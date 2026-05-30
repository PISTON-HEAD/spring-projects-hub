package com.healthcare.doctor_service.controller;

import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.doctor_service.dto.CreateDoctorRequest;
import com.healthcare.doctor_service.service.DoctorService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService service;
    
    @PostMapping
    public ResponseEntity<?> createDoctor(@Valid @RequestBody CreateDoctorRequest request)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(service.createDoctor(request));
    }

    @GetMapping("/${doctorId}")
    public ResponseEntity<?> getSlotsByDoctor(UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getSlotsByDoctor(doctorId));
    }

    @GetMapping("/${doctorId}/available")
    public ResponseEntity<?> getAvailableSlotsByDoctor(UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getAvailableSlots(doctorId));
    }

    @GetMapping("/${doctorId}/exist")
    public ResponseEntity<?> doctorExist(UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.doctorExists(doctorId));
    }

    @GetMapping
    public ResponseEntity<?> getAllDoctors(UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getAllDoctors());
    }



}

package com.healthcare.doctor_service.controller;

import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;import org.springframework.web.bind.annotation.RestController;

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

    // GET /api/doctors/{doctorId}/slots
    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<?> getSlotsByDoctor(@PathVariable UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getSlotsByDoctor(doctorId));
    }

    // GET /api/doctors/{doctorId}/slots/available
    @GetMapping("/{doctorId}/slots/available")
    public ResponseEntity<?> getAvailableSlotsByDoctor(@PathVariable UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getAvailableSlots(doctorId));
    }

    // GET /api/doctors/{doctorId}/exists
    @GetMapping("/{doctorId}/exists")
    public ResponseEntity<?> doctorExist(@PathVariable UUID doctorId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.doctorExists(doctorId));
    }

    // GET /api/doctors
    @GetMapping
    public ResponseEntity<?> getAllDoctors()
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.getAllDoctors());
    }

    // PUT /api/doctors/slots/{slotId}/reserve?appointmentId=xxx
    @PutMapping("/slots/{slotId}/reserve")
    public ResponseEntity<?> reserveSlot(@PathVariable UUID slotId, @RequestParam UUID appointmentId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.reserveSlot(slotId, appointmentId));
    }

    // PUT /api/doctors/slots/{slotId}/confirm
    @PutMapping("/slots/{slotId}/confirm")
    public ResponseEntity<?> confirmSlot(@PathVariable UUID slotId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.confirmSlot(slotId));
    }

    // PUT /api/doctors/slots/{slotId}/release
    @PutMapping("/slots/{slotId}/release")
    public ResponseEntity<?> releaseSlot(@PathVariable UUID slotId)
    {
        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(service.releaseSlot(slotId));
    }

}

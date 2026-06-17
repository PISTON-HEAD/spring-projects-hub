package com.healthcare.billing_service.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.billing_service.dto.CreatePaymentRequest;
import com.healthcare.billing_service.dto.PaymentResponse;
import com.healthcare.billing_service.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    // GET /api/payments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // GET /api/payments/appointment/{appointmentId}?page=0&size=10
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentService.getPaymentsByAppointment(appointmentId, page, size));
    }

    // GET /api/payments/patient/{patientId}?page=0&size=10
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByPatient(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(paymentService.getPaymentsByPatient(patientId, page, size));
    }

    // PUT /api/payments/{id}/process
    @PutMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.processPayment(id));
    }

    // PUT /api/payments/{id}/refund
    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }
}

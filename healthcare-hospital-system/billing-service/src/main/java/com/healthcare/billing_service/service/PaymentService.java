package com.healthcare.billing_service.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.healthcare.billing_service.dto.CreatePaymentRequest;
import com.healthcare.billing_service.dto.PaymentResponse;
import com.healthcare.billing_service.entity.Payment;
import com.healthcare.billing_service.enums.PaymentStatus;
import com.healthcare.billing_service.exception.PaymentNotFoundException;
import com.healthcare.billing_service.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    @CacheEvict(value = "payments", allEntries = true)
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .appointmentId(request.appointmentId())
                .patientId(request.patientId())
                .amount(request.amount())
                .currency(request.currency() != null ? request.currency() : "INR")
                .status(PaymentStatus.PENDING)
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    @Cacheable(value = "payment", key = "#id")
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return toResponse(payment);
    }

    @Transactional
    @Cacheable(value = "paymentsByAppointment", key = "#appointmentId + '-' + #page + '-' + #size")
    public Page<PaymentResponse> getPaymentsByAppointment(UUID appointmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentRepository.findByAppointmentId(appointmentId, pageable).map(this::toResponse);
    }

    @Transactional
    @Cacheable(value = "paymentsByPatient", key = "#patientId + '-' + #page + '-' + #size")
    public Page<PaymentResponse> getPaymentsByPatient(UUID patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return paymentRepository.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "payment", key = "#id"),
        @CacheEvict(value = "paymentsByAppointment", allEntries = true),
        @CacheEvict(value = "paymentsByPatient", allEntries = true)
    })
    public PaymentResponse processPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Payment is not in PENDING state");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "payment", key = "#id"),
        @CacheEvict(value = "paymentsByAppointment", allEntries = true),
        @CacheEvict(value = "paymentsByPatient", allEntries = true)
    })
    public PaymentResponse refundPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getAppointmentId(),
                payment.getPatientId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}

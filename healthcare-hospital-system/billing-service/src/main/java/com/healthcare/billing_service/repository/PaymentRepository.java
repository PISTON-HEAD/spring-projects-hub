package com.healthcare.billing_service.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcare.billing_service.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByAppointmentId(UUID appointmentId, Pageable pageable);

    Page<Payment> findByPatientId(UUID patientId, Pageable pageable);
}

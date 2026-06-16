package com.healthcare.billing_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.healthcare.billing_service.enums.PaymentStatus;

public record PaymentResponse(

        UUID id,
        UUID appointmentId,
        UUID patientId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

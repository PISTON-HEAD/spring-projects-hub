package com.healthcare.billing_service.enums;

public enum PaymentStatus {
    PENDING,    // payment created, not yet processed
    SUCCESS,    // payment processed successfully
    FAILED,     // payment processing failed
    REFUNDED    // payment was refunded
}

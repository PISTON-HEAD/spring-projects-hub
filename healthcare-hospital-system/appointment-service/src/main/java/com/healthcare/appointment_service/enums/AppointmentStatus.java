package com.healthcare.appointment_service.enums;

public enum AppointmentStatus {
    PENDING,          // appointment created, nothing confirmed yet
    SLOT_RESERVED,    // slot reserved in doctor service
    PAYMENT_PENDING,  // slot reserved, waiting for payment (Phase 2)
    CONFIRMED,        // fully confirmed
    CANCELLED,        // slot unavailable or payment failed
    COMPLETED 
}

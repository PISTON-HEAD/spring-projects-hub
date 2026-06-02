package com.healthcare.appointment_service.exceptions;

public class AppointmentNotFoundExceptions extends RuntimeException {
    public AppointmentNotFoundExceptions(String message)
    {
        super(message);
    }
}

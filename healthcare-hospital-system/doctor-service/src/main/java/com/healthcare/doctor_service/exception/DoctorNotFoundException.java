package com.healthcare.doctor_service.exception;

public class DoctorNotFoundException extends RuntimeException {

 DoctorNotFoundException(String message) {
  super(message);
 }
}

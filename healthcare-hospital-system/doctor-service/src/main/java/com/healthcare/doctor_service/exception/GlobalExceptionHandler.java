package com.healthcare.doctor_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(DoctorNotFoundException.class)
 public ResponseEntity<Map<String, Object>> handleDoctorNotFoundException(DoctorNotFoundException exception) {
  return buildErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
 }

 @ExceptionHandler(SlotNotFoundException.class)
 public ResponseEntity<Map<String, Object>> handleSlotNotFoundException(
   SlotNotFoundException exception) {
  return buildErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
 }

 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
   IllegalArgumentException exception) {
  return buildErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
 }

 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
  Map<String, String> validationErrors = new HashMap<>();

  exception.getBindingResult().getFieldErrors()
    .forEach(fieldError -> validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

  Map<String, Object> errorResponse = new HashMap<>();
  errorResponse.put("timestamp", LocalDateTime.now());
  errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
  errorResponse.put("message", "Validation failed");
  errorResponse.put("errors", validationErrors);
  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
 }

 private ResponseEntity<Map<String, Object>> buildErrorResponse(
   String message, HttpStatus code) {
  Map<String, Object> errors = new HashMap<>();
  errors.put("Exception Message", message);
  errors.put("HTTP-Status", code.value());
  errors.put("timestamp", LocalDateTime.now());

  return ResponseEntity.status(code.value()).body(errors);
 }
}

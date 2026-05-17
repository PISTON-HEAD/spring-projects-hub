package com.healthcare.patient_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler
 public ResponseEntity<Map<String, Object>> handlePatientNotFoundException(PatientNotFoundException exception) {
  Map<String, Object> error = new HashMap<>();
  error.put("timestamp", LocalDateTime.now());
  error.put("Status", HttpStatus.SC_NOT_FOUND);
  error.put("message", exception.getMessage());

  return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(error);
 }

 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
  Map<String, Object> error = new HashMap<>();
  error.put("timestamp", LocalDateTime.now());
  error.put("status", HttpStatusCode.valueOf(400));
  error.put("message", ex.getMessage());

  return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(error);
 }

 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
  Map<String, Object> error = new HashMap<>();
  Map<String, String> fieldErrors = new HashMap<>();

  ex.getBindingResult().getFieldErrors()
    .forEach(fieldError -> fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

  error.put("timestamp", LocalDateTime.now());
  error.put("status", HttpStatusCode.valueOf(400));
  error.put("message", "Validation failed");
  error.put("errors", fieldErrors);

  return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(error);
 }

}

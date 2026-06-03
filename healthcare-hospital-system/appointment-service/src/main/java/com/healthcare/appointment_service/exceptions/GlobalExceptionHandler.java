package com.healthcare.appointment_service.exceptions;

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
    
    @ExceptionHandler(AppointmentNotFoundExceptions.class)
    public ResponseEntity<Map<String, Object>> appointmentNotFound(AppointmentNotFoundExceptions exceptions)
    {
        return buildErrorResponse(exceptions, HttpStatus.NOT_FOUND);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex)
    {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("exception", ex.getClass().getSimpleName());
        errorResponse.put("Message", ex.getMessage());
        errorResponse.put("statusCode", HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

	private ResponseEntity<Map<String, Object>> buildErrorResponse(AppointmentNotFoundExceptions exceptions, HttpStatus status) {
		Map<String, Object> exMap = new HashMap<>();
        exMap.put("exception", exceptions.getClass());
        exMap.put("Message", exceptions.getMessage());
        exMap.put("statusCode", status.value());
        return ResponseEntity.status(status.value()).body(exMap);
	}
}

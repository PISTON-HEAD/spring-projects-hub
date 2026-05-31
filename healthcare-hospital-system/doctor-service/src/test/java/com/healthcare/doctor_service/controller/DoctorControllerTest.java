package com.healthcare.doctor_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.doctor_service.dto.CreateDoctorRequest;
import com.healthcare.doctor_service.dto.DoctorResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.service.DoctorService;

@WebMvcTest(DoctorController.class)
public class DoctorControllerTest {

 @Autowired
 MockMvc mockMvc;

 @MockBean
 DoctorService service;

 @Autowired
 ObjectMapper mapper;

 private Doctor doctor;

 private CreateDoctorRequest request;

 private DoctorResponse responseCreated;

 @BeforeEach
 void init() {
  doctor = Doctor.builder()
    .id(UUID.randomUUID())
    .firstName("Crack")
    .lastName("Bot")
    .email("crackeez@gmail.com")
    .specialization("Cardiologist")
    .phoneNumber("123-456-890")
    .active(true)
    .createdAt(LocalDateTime.now())
    .build();

  request = new CreateDoctorRequest(doctor.getFirstName(),
    doctor.getLastName(), doctor.getSpecialization(),
    doctor.getEmail(), doctor.getPhoneNumber());

  responseCreated = new DoctorResponse(doctor.getId(), doctor.getFirstName(), doctor.getLastName(),
    doctor.getSpecialization(),
    doctor.getEmail(), doctor.getPhoneNumber(), doctor.getActive(), doctor.getCreatedAt());

 }

 @Test
 void createDoctorController() throws JsonProcessingException, Exception {

  when(service.createDoctor(any(CreateDoctorRequest.class))).thenReturn(responseCreated);

  mockMvc
    .perform(post("/api/doctors").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.firstName").value(responseCreated.firstName()))
    .andExpect(jsonPath("$.specialization").value(responseCreated.specialization()));

  verify(service).createDoctor(any(CreateDoctorRequest.class));
 }
}

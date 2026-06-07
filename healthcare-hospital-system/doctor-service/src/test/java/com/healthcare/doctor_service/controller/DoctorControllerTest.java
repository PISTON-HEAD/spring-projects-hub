package com.healthcare.doctor_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.doctor_service.dto.CreateDoctorRequest;
import com.healthcare.doctor_service.dto.CreateDoctorSlotRequest;
import com.healthcare.doctor_service.dto.DoctorResponse;
import com.healthcare.doctor_service.dto.DoctorSlotResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.entity.DoctorSlots;
import com.healthcare.doctor_service.enums.SlotStatus;
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

  private DoctorSlots slot;

  private CreateDoctorSlotRequest doctorSlotRequest;

  private DoctorSlotResponse doctorSlotResponse;

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

    LocalDateTime timeNow = LocalDateTime.of(1999, 12, 15, 12, 50, 10);
    LocalDateTime endTime = LocalDateTime.of(2000, 12, 23, 22, 10, 45);

    slot = DoctorSlots.builder()
        .doctor(doctor)
        .startTime(timeNow)
        .endTime(endTime)
        .status(SlotStatus.AVAILABLE)
        .build();

    doctorSlotRequest = new CreateDoctorSlotRequest(doctor.getId(), timeNow, endTime);

    UUID slotId = UUID.randomUUID();
    UUID appointId = UUID.randomUUID();

    doctorSlotResponse = new DoctorSlotResponse(slotId, doctor.getId(), doctor.getFirstName(), timeNow, endTime,
        SlotStatus.AVAILABLE, appointId);

  }

  @Test
  void createDoctorController() throws JsonProcessingException, Exception {

    when(service.createDoctor(any(CreateDoctorRequest.class))).thenReturn(responseCreated);

    mockMvc.perform(post("/api/doctors")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.firstName").value(responseCreated.firstName()))
        .andExpect(jsonPath("$.specialization").value(responseCreated.specialization()));

    verify(service).createDoctor(any(CreateDoctorRequest.class));
  }

  @Test
  void createSlotController() throws JsonProcessingException, Exception {

    when(service.createDoctorSlot(doctor.getId(), doctorSlotRequest)).thenReturn(doctorSlotResponse);

    mockMvc.perform(post("/api/doctors/" + doctor.getId() + "/slots")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(doctorSlotRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.firstName").value(doctor.getFirstName()))
        .andExpect(jsonPath("$.startTime").value(doctorSlotResponse.startTime().toString()));

    verify(service).createDoctorSlot(doctor.getId(), doctorSlotRequest);
  }

  @Test
  void getSlotsByDoctorController() throws Exception {
    List<DoctorSlotResponse> doctorSlotReponses = List.of(doctorSlotResponse);
    Page<DoctorSlotResponse> doctorPages = new PageImpl<>(doctorSlotReponses);

    when(service.getSlotsByDoctor(doctor.getId(), 0, 10)).thenReturn(doctorPages);

    mockMvc.perform(
        get("/api/doctors/" + doctor.getId() + "/slots"))
        .andExpect(status().isOk());

    verify(service).getSlotsByDoctor(doctor.getId(), 0, 10);
  }

  @Test
  void getAvailableSlotsByDoctor() throws Exception
  {
    UUID pid = UUID.randomUUID();

    List<DoctorSlotResponse> doctorSlotReponses = List.of(doctorSlotResponse);
    Page<DoctorSlotResponse> doctorPages = new PageImpl<>(doctorSlotReponses);

    when(service.getAvailableSlots(doctor.getId(), 0, 10)).thenReturn(doctorPages);

    mockMvc.perform(get("/api/doctors/"+doctor.getId()+"/slots/available"))
    .andExpect(status().isOk());

    verify(service).getAvailableSlots(doctor.getId(),0,10);
  }
}

package com.healthcare.patient_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.patient_service.dto.CreatePatientRequest;
import com.healthcare.patient_service.dto.CreatePatientResponse;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.entity.PatientAddress;
import com.healthcare.patient_service.enums.PatientGender;
import com.healthcare.patient_service.service.PatientService;

@WebMvcTest(PatientController.class)// this is used when we want start a mini spring context for testing pupose
public class PatientControllerTest {

    @Autowired // the mockMVC is used to simulate the HTTP calls instead of startin a real server
    MockMvc mockMvc;

    @MockBean // we use MockBean insead of Mock because we are using a mini spring context with help of WebMvcTest and the new bean also needs to be registered there only
    //usin just mock will leave the patient service as null as we are using a mini spring context with help of WebMVCTest
    PatientService service;

    @Autowired // converts ur request (obj) to JSON
    ObjectMapper mapper;

    @Test
    void createPatientHTTPCall() throws Exception
    {
        PatientAddress address = new PatientAddress();
        address.setCountry("India");
        address.setState("Kerala");
        CreatePatientRequest request = new CreatePatientRequest(
            "Rahul", "Sharma", 30, PatientGender.MALE,
            "rahul@example.com", "9999999999", address
        );
        CreatePatientResponse response = new CreatePatientResponse( UUID.randomUUID(), "Rahul", "Sharma", "rahul@example.com", LocalDateTime.now());
        
        when(service.createPatient(any(CreatePatientRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("rahul@example.com"))
            .andExpect(jsonPath("$.firstName").value("Rahul"));

        verify(service).createPatient(any(CreatePatientRequest.class));
    }

    @Test
    void getPatientByIdTest() throws Exception
    {
        UUID uuid = UUID.randomUUID();
        PatientAddress address = new PatientAddress();
        address.setCountry("India");
        address.setState("Kerala");
        
        Patient request = Patient.builder()
        .id(uuid)
        .address(address)
        .age(30)
        .email("crackeez@example.com")
        .firstName("Crack")
        .lastName("Bot")
        .phoneNumber("9999999999")
        .gender(PatientGender.MALE)
        .build();

        CreatePatientResponse response = new CreatePatientResponse(uuid, request.getFirstName(), request.getLastName(), request.getEmail(), LocalDateTime.now());
        
        
        when(service.getPatientById(uuid)).thenReturn(response);

        mockMvc.perform(get("/api/patients/" + uuid))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("crackeez@example.com"))
        .andExpect(jsonPath("$.firstName").value("Crack"));

        verify(service).getPatientById(uuid);
    }

    @Test
    void existsByIdTest() throws Exception
    {
        UUID id = UUID.randomUUID();
        when(service.existsById(id)).thenReturn(true);

        mockMvc.perform(get("/api/patients/" + id + "/exists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true));

        verify(service).existsById(id);
    }
    
}

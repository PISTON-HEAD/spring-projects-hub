package com.healthcare.patient_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.healthcare.patient_service.service.PatientService;

@WebMvcTest(PatientController.class)// this is used when we want start a mini spring context for testing pupose
public class PatientController {

@Autowired // the mockMVC is used to simulate the HTTP calls instead of startin a real server
MockMvc mockMvc;

@MockBean // we use MockBean insead of Mock because we are using a mini spring context with help of WebMvcTest and the new bean also needs to be registered there only
//usin just mock will leave the patient service as null as we are using a mini spring context with help of WebMVCTest
PatientService service;

    @Test
    void createPatientHTTPCall()
    {

    }
    
}

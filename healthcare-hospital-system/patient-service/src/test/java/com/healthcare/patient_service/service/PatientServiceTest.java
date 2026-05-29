package com.healthcare.patient_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.healthcare.patient_service.dto.CreatePatientRequest;
import com.healthcare.patient_service.dto.CreatePatientResponse;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.entity.PatientAddress;
import com.healthcare.patient_service.enums.PatientGender;
import com.healthcare.patient_service.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void createPatientTest() {
        PatientAddress address = new PatientAddress();
        CreatePatientRequest request = new CreatePatientRequest(
            "Rahul", "Sharma", 30, PatientGender.MALE,
            "rahul@example.com", "9999999999", address
        );

        Patient savedPatient = Patient.builder()
            .id(UUID.randomUUID())
            .address(address)
            .age(30)
            .email("rahul@example.com")
            .firstName("Rahul")
            .lastName("Sharma")
            .phoneNumber("9999999999")
            .gender(PatientGender.MALE)
            .build();

        when(patientRepository.existsByEmail(request.email())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        CreatePatientResponse response = (CreatePatientResponse) patientService.createPatient(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(request.email(), response.email());
        Assertions.assertEquals(request.firstName(), response.firstName());

        verify(patientRepository).existsByEmail(request.email());
        verify(patientRepository).save(any(Patient.class));
    }
}

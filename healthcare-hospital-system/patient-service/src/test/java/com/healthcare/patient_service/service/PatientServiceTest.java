package com.healthcare.patient_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
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

    @Test
    void getPatientByIdTest(){
        UUID id = UUID.randomUUID();
        PatientAddress address = new PatientAddress();
        Optional<Patient> samePatient =  Optional.of(Patient.builder().id(id).address(address).age(25).createdAt(LocalDateTime.now()).email("dragon@gmail.com").gender(PatientGender.MALE).lastName("dragon").firstName("Mighty").build());
        when(patientRepository.findById(id)).thenReturn(samePatient);
        CreatePatientResponse savedPatient = patientService.getPatientById(id);
        Assertions.assertEquals(samePatient.get().getEmail(), savedPatient.email());
        Assertions.assertEquals(samePatient.get().getId(), savedPatient.id());
        Assertions.assertEquals(samePatient.get().getFirstName(), savedPatient.firstName());
        Assertions.assertEquals(samePatient.get().getLastName(), savedPatient.lastName());
    
        verify(patientRepository).findById(id);
    }

    @Test
    void existsByIdTest()
    {
        UUID id = UUID.randomUUID();
        when(patientRepository.existsById(id)).thenReturn(true);

        Boolean isPatientThere = patientService.existsById(id);
        
        Assertions.assertEquals(true,isPatientThere);
        verify(patientRepository).existsById(id);
    }
}

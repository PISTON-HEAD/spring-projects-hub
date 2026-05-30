package com.healthcare.patient_service.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.healthcare.patient_service.dto.CreatePatientRequest;
import com.healthcare.patient_service.dto.CreatePatientResponse;
import com.healthcare.patient_service.dto.UpdatePatient;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.entity.PatientAddress;
import com.healthcare.patient_service.enums.PatientGender;
import com.healthcare.patient_service.exception.PatientNotFoundException;
import com.healthcare.patient_service.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientAddress address;

    @BeforeEach
    void init() {
        address = new PatientAddress();
        address.setCountry("India");
        address.setState("Kerala");
    }


    @Test
    void createPatientTest() {
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

    @Test
    void updatePatientTest()
    {
        UUID id = UUID.randomUUID();
        Optional<Patient> existingPatient = Optional.of(Patient.builder()
        .id(id)
        .firstName("Mighty")
        .lastName("dragon")
        .age(25)
        .email("dragon@gmail.com")
        .gender(PatientGender.MALE)
        .address(address)
        .createdAt(LocalDateTime.now())
        .build());

        when(patientRepository.findById(id)).thenReturn(existingPatient);
        
        UpdatePatient updatePatient = new UpdatePatient("UpdatedName", null, 33, null, null, null, null);
        Patient updatedPatient = Patient.builder()
        .id(id)
        .firstName("UpdatedName")  // updated
        .lastName("dragon")        // unchanged
        .age(33)                   // updated
        .email("dragon@gmail.com") // unchanged
        .gender(PatientGender.MALE)
        .address(address)
        .createdAt(existingPatient.get().getCreatedAt())
        .build();

        when(patientRepository.findById(id)).thenReturn(existingPatient);
        when(patientRepository.saveAndFlush(any(Patient.class))).thenReturn(updatedPatient);

        CreatePatientResponse response = patientService.updatePatient(id, updatePatient);

        Assertions.assertEquals(updatedPatient.getFirstName(), response.firstName());
        Assertions.assertEquals(updatedPatient.getLastName() , response.lastName());
        verify(patientRepository).findById(id);
        verify(patientRepository).saveAndFlush(any(Patient.class));

    }

    @Test
    void PatientNotFoundExceptionTest()
    {
        UUID id = UUID.randomUUID();
        when(patientRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class,() -> patientService.getPatientById(id));

        verify(patientRepository).findById(id);
    }

    @Test
    void createPatientExceptionTest()
    {
        CreatePatientRequest request = new CreatePatientRequest(
            "Rahul", "Sharma", 30, PatientGender.MALE,
            "rahul@example.com", "9999999999", address
        );
        when(patientRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, ()-> patientService.createPatient(request));

        verify(patientRepository).existsByEmail(request.email());
    }
}

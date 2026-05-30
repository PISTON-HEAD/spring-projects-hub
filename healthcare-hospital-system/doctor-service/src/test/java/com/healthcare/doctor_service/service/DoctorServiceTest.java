package com.healthcare.doctor_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import com.healthcare.doctor_service.dto.CreateDoctorRequest;
import com.healthcare.doctor_service.dto.DoctorResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    DoctorRepository repository;

    @InjectMocks
    DoctorService service;

    private Doctor doctor;

    @BeforeEach
    public void init_beforeEaach()
    {
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
    }

    @Test
    void createDoctorTest()
    {
        UUID id = UUID.randomUUID();
        CreateDoctorRequest request = new CreateDoctorRequest("Crack", "Bot", "Alchoholist", "crackeez@gmail.com", "123-456-890");
        Doctor dc = Doctor.builder().active(true).createdAt(LocalDateTime.now()).email(request.email()).firstName(request.firstName()).id(id).lastName(request.lastName()).specialization(request.specialization()).build();

        when(repository.existsByEmail(dc.getEmail())).thenReturn(false);
        when(repository.save(any(Doctor.class))).thenReturn(dc);

        DoctorResponse dr = service.createDoctor(request);

        Assertions.assertEquals(request.email(),dr.email());
        Assertions.assertEquals(request.firstName(), dr.firstName());
        Assertions.assertEquals(request.lastName(), dr.lastName());

        verify(repository).existsByEmail(dc.getEmail());
        verify(repository).save(any(Doctor.class));
    }

    @Test
    void getDoctorByIdTest()
    {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(doctor));

        DoctorResponse response = service.getDoctorById(id);

        Assertions.assertEquals(doctor.getFirstName(), response.firstName());
        Assertions.assertEquals(doctor.getLastName(), response.lastName());
        Assertions.assertEquals(doctor.getSpecialization(), response.specialization());
    }

    @Test
    void testProvateMethodDoctorReponse() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException
    {
        Method tDoctorResponse = DoctorService.class.getDeclaredMethod("tDoctorResponse", Doctor.class);
        tDoctorResponse.setAccessible(true);

        DoctorResponse result = (DoctorResponse) tDoctorResponse.invoke(service, doctor);
        
        Assertions.assertEquals("crackeez@gmail.com", result.email());
        Assertions.assertEquals("Crack", result.firstName());


    }
}

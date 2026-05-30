package com.healthcare.doctor_service.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
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
import com.healthcare.doctor_service.dto.CreateDoctorSlotRequest;
import com.healthcare.doctor_service.dto.DoctorResponse;
import com.healthcare.doctor_service.dto.DoctorSlotResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.entity.DoctorSlots;
import com.healthcare.doctor_service.exception.DoctorNotFoundException;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.repository.DoctorSlotRepository;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    DoctorRepository repository;

    @Mock
    DoctorSlotRepository slotRepository;

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
    void getDoctorById_ThrowsException()
    {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> service.getDoctorById(id));
        
        verify(repository).findById(id);
    }

    @Test
    void testPrivateMethodDoctorReponse() throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException
    {
        Method tDoctorResponse = DoctorService.class.getDeclaredMethod("tDoctorResponse", Doctor.class);
        tDoctorResponse.setAccessible(true);

        DoctorResponse result = (DoctorResponse) tDoctorResponse.invoke(service, doctor);
        
        Assertions.assertEquals("crackeez@gmail.com", result.email());
        Assertions.assertEquals("Crack", result.firstName());

    }

    @Test
    void createDoctorSlotTest()
    {
        UUID id = UUID.randomUUID();
        LocalDateTime timeNow = LocalDateTime.of(1999, 12, 15, 12, 50, 10);
        LocalDateTime endTime = LocalDateTime.of(2000, 12, 23, 22, 10, 45);
        CreateDoctorSlotRequest request = new CreateDoctorSlotRequest(id, timeNow,endTime);

        DoctorSlots slot = DoctorSlots.builder().doctor(doctor).createdAt(timeNow).endTime(endTime).build();

        when(repository.findById(id)).thenReturn(Optional.of(doctor));        
        when(slotRepository.save(any(DoctorSlots.class))).thenReturn(slot);

        DoctorSlotResponse response = service.createDoctorSlot(id, request);

        Assertions.assertEquals(response.firstName(), doctor.getFirstName());
        Assertions.assertEquals(response.endTime(), request.endTime());


        verify(slotRepository).save(any(DoctorSlots.class));
        verify(repository).findById(id);
    }

    @Test
    void getSlotsByDoctorTest()
    {
        UUID id = UUID.randomUUID();

        DoctorSlots slot1 = DoctorSlots.builder()
            .id(UUID.randomUUID())
            .doctor(doctor)
            .startTime(LocalDateTime.of(2025, 1, 10, 9, 0))
            .endTime(LocalDateTime.of(2025, 1, 10, 10, 0))
            .build();

        DoctorSlots slot2 = DoctorSlots.builder()
            .id(UUID.randomUUID())
            .doctor(doctor)
            .startTime(LocalDateTime.of(2025, 1, 10, 11, 0))
            .endTime(LocalDateTime.of(2025, 1, 10, 12, 0))
            .build();

        DoctorSlots slot3 = DoctorSlots.builder()
            .id(UUID.randomUUID())
            .doctor(doctor)
            .startTime(LocalDateTime.of(2025, 1, 10, 14, 0))
            .endTime(LocalDateTime.of(2025, 1, 10, 15, 0))
            .build();

        when(slotRepository.findByDoctorId(id)).thenReturn(List.of(slot1, slot2, slot3));

        List<DoctorSlotResponse> slots = service.getSlotsByDoctor(id);

        Assertions.assertEquals(3, slots.size());
        Assertions.assertEquals(slot1.getStartTime(), slots.get(0).startTime());
        Assertions.assertEquals(slot2.getStartTime(), slots.get(1).startTime());

        verify(slotRepository).findByDoctorId(id);
    }

    @Test
    void getAvailableSlotsTest()
    {

    }
}

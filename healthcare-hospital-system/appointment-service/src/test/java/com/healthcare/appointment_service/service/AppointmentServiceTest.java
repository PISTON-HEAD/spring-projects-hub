package com.healthcare.appointment_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.healthcare.appointment_service.dto.AppointmentResponse;
import com.healthcare.appointment_service.dto.CreateAppointmentRequest;
import com.healthcare.appointment_service.entity.Appointment;
import com.healthcare.appointment_service.repository.AppointmentRepository;
import com.healthcare.doctor_service.grpc.DoctorGrpcServiceGrpc;
import com.healthcare.patient_service.grpc.PatientGrpcServiceGrpc;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

 @Mock
 private AppointmentRepository repository;

 @Mock
 private PatientGrpcServiceGrpc.PatientGrpcServiceBlockingStub patientStub;

 @Mock
 private DoctorGrpcServiceGrpc.DoctorGrpcServiceBlockingStub doctorStub;

 @InjectMocks
 private AppointmentService service;

 private Appointment appointment;

 private CreateAppointmentRequest request;

 @BeforeEach
 public void beforeEach() {
  UUID patientId = UUID.randomUUID();
  UUID doctorId = UUID.randomUUID();
  UUID slotId = UUID.randomUUID();

  LocalDateTime now = LocalDateTime.of(2002, 8, 12, 8, 40, 00);

  appointment = Appointment.builder()
    .patientId(patientId)
    .doctorId(doctorId)
    .slotId(slotId)
    .reason("no reason")
    .appointmentDateTime(now)
    .build();

  request = new CreateAppointmentRequest(patientId, doctorId, slotId, "no reason", now);
 }

 @Test
 void createAppointment() {
  when(repository.save(any(Appointment.class))).thenReturn(appointment);

  AppointmentResponse createdAppointment = service.createAppointment(request);

  Assertions.assertEquals(request.appointmentTime(), createdAppointment.appointmentDateTime());
  verify(repository).save(any(Appointment.class));
 }

}

package com.healthcare.appointment_service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.healthcare.appointment_service.dto.AppointmentResponse;
import com.healthcare.appointment_service.dto.CreateAppointmentRequest;
import com.healthcare.appointment_service.entity.Appointment;
import com.healthcare.appointment_service.repository.AppointmentRepository;
import com.healthcare.doctor_service.grpc.DoctorGrpcServiceGrpc;
import com.healthcare.doctor_service.grpc.DoctorResponse;
import com.healthcare.patient_service.grpc.PatientGrpcServiceGrpc;
import com.healthcare.patient_service.grpc.PatientResponse;

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

  ReflectionTestUtils.setField(service, "patientStub", patientStub);
  ReflectionTestUtils.setField(service, "doctorStub", doctorStub);

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
  when(patientStub.checkPatientExists(any())).thenReturn(
        PatientResponse.newBuilder().setExists(true).build());
  when(doctorStub.checkDoctorExists(any())).thenReturn(DoctorResponse.newBuilder().setExists(true).build());
  when(repository.save(any(Appointment.class))).thenReturn(appointment);

  AppointmentResponse createdAppointment = service.createAppointment(request);

  Assertions.assertEquals(request.appointmentTime(), createdAppointment.appointmentDateTime());
  verify(repository).save(any(Appointment.class));
 }


 @Test
 void getAppointmentByIdTest()
 {
  UUID id = UUID.randomUUID();
  when(repository.findById(id)).thenReturn(Optional.of(appointment));

  AppointmentResponse response = service.getAppointmentById(id);

  Assertions.assertEquals(appointment.getAppointmentDateTime(), response.appointmentDateTime());

  verify(repository).findById(id);
 }

 @Test
 void getDoctorById()
 {
  UUID doctorId = UUID.randomUUID();
  List<Appointment> appointments = List.of(appointment);
  Page<Appointment> appList = new PageImpl<>(appointments);
  when(repository.findByDoctorId(any(UUID.class), any(Pageable.class))).thenReturn(appList);
  
  Page<AppointmentResponse> saveAppoint = service.getAppointmentsByDoctor(doctorId, 0, 10);
  
  Assertions.assertEquals(appointments.get(0).getAppointmentDateTime(), saveAppoint.getContent().get(0).appointmentDateTime());

  verify(repository).findByDoctorId(any(UUID.class), any(Pageable.class));
 }

}

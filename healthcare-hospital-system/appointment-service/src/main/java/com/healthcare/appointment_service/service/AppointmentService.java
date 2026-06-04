package com.healthcare.appointment_service.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.healthcare.appointment_service.dto.AppointmentResponse;
import com.healthcare.appointment_service.dto.CreateAppointmentRequest;
import com.healthcare.appointment_service.entity.Appointment;
import com.healthcare.appointment_service.exceptions.AppointmentNotFoundExceptions;
import com.healthcare.appointment_service.repository.AppointmentRepository;
import com.healthcare.doctor_service.grpc.DoctorGrpcServiceGrpc;
import com.healthcare.doctor_service.grpc.DoctorRequest;
import com.healthcare.doctor_service.grpc.DoctorResponse;
import com.healthcare.patient_service.grpc.PatientGrpcServiceGrpc;
import com.healthcare.patient_service.grpc.PatientRequest;
import com.healthcare.patient_service.grpc.PatientResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository repository;

    @GrpcClient("patient-service")
    private PatientGrpcServiceGrpc.PatientGrpcServiceBlockingStub patientStub;

    @GrpcClient("doctor-service")
    private DoctorGrpcServiceGrpc.DoctorGrpcServiceBlockingStub doctorStub;

    @Transactional
    @CacheEvict(allEntries = true, value = "appointments")
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        PatientResponse patientResponse = patientStub.checkPatientExists(PatientRequest.newBuilder()
                .setPatientId(request.patientId().toString())
                .build());
        if (!patientResponse.getExists()) {
            throw new IllegalArgumentException("Patient not found: " + request.patientId());
        }

        DoctorResponse doctorResponse = doctorStub.checkDoctorExists(
                DoctorRequest.newBuilder()
                        .setDoctorId(request.doctorId().toString())
                        .build());
        if (!doctorResponse.getExists()) {
            throw new IllegalArgumentException("Doctor not found: " + request.doctorId());
        }

        Appointment appointment = Appointment.builder()
                .patientId(request.patientId())
                .doctorId(request.doctorId())
                .slotId(request.slotId())
                .reason(request.reason())
                .appointmentDateTime(request.appointmentTime())
                .build();

        Appointment saved = repository.save(appointment);
        return toResponse(saved);
    }

    @Transactional
    @Cacheable(value = "appointment", key = "#appointmentId")
    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        Appointment appointment = repository.findById(appointmentId)
                .orElseThrow(
                        () -> new AppointmentNotFoundExceptions("Appointment not found with id: " + appointmentId));
        return toResponse(appointment);
    }

    @Transactional
    @Cacheable(value = "appointments", key = "#patientId + '-' + #page + '-' + #size")
    public Page<AppointmentResponse> getAppointmentsByPatient(UUID patientId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    @Transactional
    @Cacheable(value = "doctorAppointments", key = "#doctorId + '-' + #page + '-' + #size")
    public Page<AppointmentResponse> getAppointmentsByDoctor(UUID doctorId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repository.findByDoctorId(doctorId, pageable).map(this::toResponse);
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(), a.getPatientId(), a.getDoctorId(), a.getSlotId(),
                a.getStatus(), a.getReason(), a.getNotes(),
                a.getAppointmentDateTime(), a.getCreatedAt());
    }
}

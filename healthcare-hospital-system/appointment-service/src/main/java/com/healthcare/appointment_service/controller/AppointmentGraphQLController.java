package com.healthcare.appointment_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.healthcare.appointment_service.dto.AppointmentResponse;
import com.healthcare.appointment_service.dto.CreateAppointmentRequest;
import com.healthcare.appointment_service.service.AppointmentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AppointmentGraphQLController {
    
    private final AppointmentService service;

    @QueryMapping
    public AppointmentResponse getAppointmentById(@Argument UUID appointmentId)
    {
        return service.getAppointmentById(appointmentId);
    }

    @QueryMapping(name = "getAppointmentByPatient")
    public List<AppointmentResponse> getAppointmentsByPatient(
        @Argument UUID patientId, @Argument int page, @Argument int size) { 
            return service.getAppointmentsByPatient(patientId, page, size).getContent();
    }

    @QueryMapping
    public List<AppointmentResponse> getAppointmentsByDoctor(
        @Argument UUID doctorId, @Argument int page, @Argument int size) {
    return service.getAppointmentsByDoctor(doctorId, page, size).getContent();

    }

    @MutationMapping
    public AppointmentResponse createAppointment(@Argument CreateAppointmentRequest request)
    {
        return service.createAppointment(request);
    }

}

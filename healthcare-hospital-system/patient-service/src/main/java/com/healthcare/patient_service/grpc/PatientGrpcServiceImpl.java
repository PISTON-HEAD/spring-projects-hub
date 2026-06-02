package com.healthcare.patient_service.grpc;

import com.healthcare.patient_service.repository.PatientRepository;

import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@AllArgsConstructor
public class PatientGrpcServiceImpl{

    private final PatientRepository patientRepository;


}

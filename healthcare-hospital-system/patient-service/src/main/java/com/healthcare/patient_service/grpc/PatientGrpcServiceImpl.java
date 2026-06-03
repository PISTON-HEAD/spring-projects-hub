package com.healthcare.patient_service.grpc;


import java.util.UUID;

import com.healthcare.patient_service.repository.PatientRepository;

import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@AllArgsConstructor
public class PatientGrpcServiceImpl extends PatientGrpcServiceGrpc.PatientGrpcServiceImplBase{

    private final PatientRepository patientRepository;

    public void checkPatientExists(PatientRequest request, StreamObserver<PatientResponse> responseObserver)
    {
        try{
            UUID id = UUID.fromString( request.getPatientId());

            PatientResponse response = patientRepository.findById(id)
            .map((patient) -> PatientResponse.newBuilder()
                            .setExists(true)
                            .setPatientId(patient.getId().toString())
                            .setName(patient.getFirstName() + " " + patient.getLastName())
                            .build())
                    .orElseGet(() -> PatientResponse.newBuilder()
                            .setExists(false)
                            .setPatientId(request.getPatientId())
                            .setName("")
                            .build());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }catch(IllegalArgumentException e)
        {
            responseObserver.onError(
                io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid UUID format: " + request.getPatientId())
                    .asRuntimeException()
            );
        }
    }

}

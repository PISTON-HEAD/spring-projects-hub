package com.healthcare.doctor_service.grpc;

import java.util.UUID;

import com.healthcare.doctor_service.entity.DoctorSlots;
import com.healthcare.doctor_service.enums.SlotStatus;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.repository.DoctorSlotRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@AllArgsConstructor
public class DoctorServiceGrpcImpl extends DoctorGrpcServiceGrpc.DoctorGrpcServiceImplBase {

    private final DoctorRepository doctorRepository;

    @Override
    public void checkDoctorExists(DoctorRequest request, StreamObserver<DoctorResponse> responseObserver) {
        try {
            UUID id = UUID.fromString(request.getDoctorId());

            DoctorResponse response = doctorRepository.findById(id)
                    .map(doctor -> DoctorResponse.newBuilder()
                            .setExists(true)
                            .setDoctorId(doctor.getId().toString())
                            .setName(doctor.getFirstName() + " " + doctor.getLastName())
                            .build())
                    .orElseGet(() -> DoctorResponse.newBuilder()
                            .setExists(false)
                            .setDoctorId(request.getDoctorId())
                            .setName("")
                            .build());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid UUID format: " + request.getDoctorId())
                            .asRuntimeException());
        }
    }

    @Override
    public void ReserveSlot(ReserveSlotRequest request, StreamObserver<SlotResponse> responseObserver) {
        try {
            UUID slotId = UUID.randomUUID();
            UUID appointmentId = UUID.fromString(request.getSlotId());

        } catch (Exception e) {

        }
    }
}

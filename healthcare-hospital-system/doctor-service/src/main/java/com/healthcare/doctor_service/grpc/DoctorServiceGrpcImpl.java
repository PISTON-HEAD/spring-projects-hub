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

    private final DoctorSlotRepository slotRepository;

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
    public void reserveSlot(ReserveSlotRequest request, StreamObserver<SlotResponse> responseObserver) {
        try {
            UUID slotId = UUID.fromString(request.getSlotId());
            UUID appointmentId = UUID.fromString(request.getAppointmentId());

            DoctorSlots docSlot = slotRepository.findById(slotId).orElseThrow(
                () -> new RuntimeException("Slot not found: "+slotId)
            );

            if(docSlot.getStatus() != SlotStatus.AVAILABLE)
            {
                responseObserver.onError(
                Status.FAILED_PRECONDITION
                    .withDescription("Slot is not available for reservation")
                    .asRuntimeException());
                return;
            }

            docSlot.setStatus(SlotStatus.RESERVED);
            docSlot.setReservedByAppointmentId(appointmentId);
            slotRepository.save(docSlot);

            SlotResponse response = SlotResponse.newBuilder()
            .setSlotId(docSlot.getId().toString())
            .setDoctorId(docSlot.getDoctor().getId().toString())
            .setStatus(docSlot.getStatus().toString())
            .setAppointmentId(appointmentId.toString())
            .setStartTime(docSlot.getStartTime().toString())
            .setEndTime(docSlot.getEndTime().toString())
            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException()
            );
        }
    }

    @Override
    public void confirmSlot(SlotRequest request, StreamObserver<SlotResponse> responseObserver)
    {
        try {
            UUID slotId = UUID.fromString(request.getSlotId());

            DoctorSlots docSlot = slotRepository.findById(slotId).orElseThrow(
                () -> new RuntimeException("Slot not found: " + slotId)
            );

            if (docSlot.getStatus() != SlotStatus.RESERVED) {
                responseObserver.onError(
                    Status.FAILED_PRECONDITION
                    .withDescription("Slot is not reserved and cannot be confirmed")
                    .asRuntimeException()
                );
                return;
            }

            docSlot.setStatus(SlotStatus.CONFIRMED);
            slotRepository.save(docSlot);

            SlotResponse response = SlotResponse.newBuilder()
                .setSlotId(docSlot.getId().toString())
                .setDoctorId(docSlot.getDoctor().getId().toString())
                .setStatus(docSlot.getStatus().name())
                .setAppointmentId(docSlot.getReservedByAppointmentId() != null ? docSlot.getReservedByAppointmentId().toString() : "")
                .setStartTime(docSlot.getStartTime().toString())
                .setEndTime(docSlot.getEndTime().toString())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException()
            );
        }
    }


    @Override
    public void releaseSlot(SlotRequest request, StreamObserver<SlotResponse> responseObserver)
    {
        try {
            UUID slotId = UUID.fromString(request.getSlotId());

            DoctorSlots docSlot = slotRepository.findById(slotId).orElseThrow(
                () -> new RuntimeException("Slot not found: " + slotId)
            );

            if (docSlot.getStatus() == SlotStatus.AVAILABLE) {
                responseObserver.onError(
                    Status.FAILED_PRECONDITION
                    .withDescription("Slot is already available")
                    .asRuntimeException()
                );
                return;
            }

            docSlot.setStatus(SlotStatus.AVAILABLE);
            docSlot.setReservedByAppointmentId(null);
            slotRepository.save(docSlot);

            SlotResponse response = SlotResponse.newBuilder()
                .setSlotId(docSlot.getId().toString())
                .setDoctorId(docSlot.getDoctor().getId().toString())
                .setStatus(docSlot.getStatus().name())
                .setAppointmentId("")
                .setStartTime(docSlot.getStartTime().toString())
                .setEndTime(docSlot.getEndTime().toString())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL
                .withDescription(e.getMessage())
                .asRuntimeException()
            );    
        }
    }
}

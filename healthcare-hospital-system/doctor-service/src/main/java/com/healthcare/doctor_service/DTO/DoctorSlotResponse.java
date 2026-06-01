package com.healthcare.doctor_service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.healthcare.doctor_service.enums.SlotStatus;

public record DoctorSlotResponse(
  UUID slotId,
  UUID doctorId,
  String firstName,
  LocalDateTime startTime,
  LocalDateTime endTime,
  SlotStatus status,
  UUID reservedByAppointmentId

) implements Serializable {

}

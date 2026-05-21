package com.healthcare.doctor_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcare.doctor_service.entity.DoctorSlots;
import com.healthcare.doctor_service.enums.SlotStatus;

public interface DoctorSlotRepository extends JpaRepository<DoctorSlots, UUID> {
 List<DoctorSlots> findByDoctorId(UUID doctorId);

 List<DoctorSlots> findByDoctorIdAndStatus(UUID doctorId, SlotStatus status);

 boolean existsByDoctorIdAndStartTimeAndEndTime(
   UUID doctorId,
   LocalDateTime startTime,
   LocalDateTime endTime);

}

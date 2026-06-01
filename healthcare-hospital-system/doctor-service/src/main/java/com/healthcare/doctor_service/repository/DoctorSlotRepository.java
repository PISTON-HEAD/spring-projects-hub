package com.healthcare.doctor_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.healthcare.doctor_service.entity.DoctorSlots;
import com.healthcare.doctor_service.enums.SlotStatus;

public interface DoctorSlotRepository extends JpaRepository<DoctorSlots, UUID> {
  Page<DoctorSlots> findByDoctorId(UUID doctorId, Pageable pageable);

  @Query("""
      SELECT ds
      FROM DoctorSlots ds
      WHERE ds.doctor.id = :doctorId
      AND ds.status = :status
      """)
  Page<DoctorSlots> findByDoctorIdAndStatus(UUID doctorId, SlotStatus status, Pageable pageable);

  boolean existsByDoctorIdAndStartTimeAndEndTime(
      UUID doctorId,
      LocalDateTime startTime,
      LocalDateTime endTime);

}

package com.healthcare.doctor_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.google.auto.value.AutoValue.Builder;
import com.healthcare.doctor_service.enums.SlotStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Table(uniqueConstraints = {
  @UniqueConstraint(name = "doctor_reservation_uniqueness", columnNames = { "doctor_id", "start_time", "end_time" })
})
public class DoctorSlots {
 @Id
 @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne
 @JoinColumn(name = "doctor_id", nullable = false)
 private Doctor doctor;

 @Column(nullable = false, name = "start_time")
 private LocalDateTime startTime;

 @Column(nullable = false, name = "end_time")
 private LocalDateTime endTime;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private SlotStatus status;

}

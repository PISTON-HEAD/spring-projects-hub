package com.healthcare.appointment_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.healthcare.appointment_service.enums.AppointmentStatus;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID patientId;

    @NotNull
    private UUID doctorId;

    @NotNull
    private UUID slotId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @NotEmpty
    private String reason;

    @Nullable
    private String notes;

    private LocalDateTime appointmentDateTime;
    
    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist()
    {
        this.createdAt = LocalDateTime.now();
        this.status = AppointmentStatus.PENDING;
    }
}

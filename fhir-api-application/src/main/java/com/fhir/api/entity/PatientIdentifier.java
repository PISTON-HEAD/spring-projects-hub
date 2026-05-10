package com.fhir.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_identifier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientIdentifier {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "identifier_id", updatable = false, nullable = false, length = 36)
    private UUID identifierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "identifier_type", nullable = false)
    private String identifierType;

    @Column(name = "identifier_value", nullable = false)
    private String identifierValue;

    @Column(name = "`system`")
    private String system;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

package com.healthcare.patient_service.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PatientAddress")
public class PatientAddress {

 @Id
 private UUID id;

 @OneToOne(fetch = FetchType.LAZY)
 @MapsId
 @JoinColumn(name = "id")
 private Patient patient;

 @NotBlank
 @Column(nullable = false)
 private String country;

 private String state;

 private String houseName;

}

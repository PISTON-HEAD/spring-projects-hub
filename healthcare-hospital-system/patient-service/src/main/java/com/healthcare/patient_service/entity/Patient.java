package com.healthcare.patient_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.healthcare.patient_service.enums.PatientGender;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Patient {

 @Id
 @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @Column(nullable = false)
 private String firstName;

 private String lastName;

 @Column(nullable = false)
 private Integer age;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private PatientGender gender;

 @Column(nullable = false, unique = true)
 private String email;

 private String phoneNumber;

 @NotNull
 @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "patient")
 private PatientAddress address;

 @Column(nullable = false)
 private LocalDateTime createdAt;

 public void setAddress(PatientAddress address) {
  this.address = address;
  if (address != null) {
   address.setPatient(this);
  }
 }

 @PrePersist
 public void prePersist() {
  this.createdAt = LocalDateTime.now();
 }

 public void setAddress(PatientAddress address) {
  this.address = address;

  if (address != null) {
   address.setPatient(this);
  }
 }
}

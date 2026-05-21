package com.healthcare.doctor_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
 @Id
 @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @Column(nullable = false)
 private String firstName;

 private String lastName;

 @Column(nullable = false)
 private String specialization;

 @Column(nullable = false, unique = true)
 private String email;

 private String phoneNumber;

 @Column(nullable = false)
 private Boolean active;

 @Column(nullable = false)
 private LocalDateTime createdAt;

 @PrePersist
 public void setCreatedAt() {
  this.createdAt = LocalDateTime.now();
  if (this.active == null) {
   this.active = true;
  }
 }
}

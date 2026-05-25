package com.healthcare.doctor_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.healthcare.doctor_service.dto.CreateDoctorRequest;
import com.healthcare.doctor_service.dto.CreateDoctorSlotRequest;
import com.healthcare.doctor_service.dto.DoctorResponse;
import com.healthcare.doctor_service.dto.DoctorSlotResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.entity.DoctorSlots;
import com.healthcare.doctor_service.enums.SlotStatus;
import com.healthcare.doctor_service.exception.DoctorNotFoundException;
import com.healthcare.doctor_service.exception.SlotNotFoundException;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.repository.DoctorSlotRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class DoctorService {

 private final DoctorRepository repository;
 private final DoctorSlotRepository doctorSlotsRepository;

 @Transactional
 public DoctorResponse createDoctor(CreateDoctorRequest request) {
  if (repository.existsByEmail(request.email())) {
   throw new IllegalArgumentException("Doctor with this email already exists");
  }

  Doctor doctor = Doctor.builder()
    .firstName(request.firstName())
    .lastName(request.lastName())
    .specialization(request.specialization())
    .email(request.email())
    .phoneNumber(request.phoneNumber())
    .active(true)
    .build();

  Doctor savedDoctor = repository.save(doctor);

  return tDoctorResponse(savedDoctor);
 }

 @Transactional
 public DoctorResponse getDoctorById(UUID doctorId) {
  Doctor doctor = repository.findById(doctorId)
    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

  return tDoctorResponse(doctor);
 }

 @Transactional
 public DoctorSlotResponse createDoctorSlot(UUID doctorId, CreateDoctorSlotRequest request) {
  Doctor doctor = repository.findById(doctorId)
    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

  DoctorSlots slot = DoctorSlots.builder()
    .doctor(doctor)
    .startTime(request.startTime())
    .endTime(request.endTime())
    .status(SlotStatus.AVAILABLE)
    .build();

  DoctorSlots savedSlot = doctorSlotsRepository.save(slot);

  return tDoctorSlotResponse(savedSlot);
 }

  @Transactional
  public List<DoctorSlotResponse> getSlotsByDoctor(UUID doctorId){
    List<DoctorSlots> slots = doctorSlotsRepository.findByDoctorId(doctorId);
    return slots.stream().map(this::tDoctorSlotResponse).toList(); 
  }

  @Transactional
  public List<DoctorSlotResponse> getAvailableSlots(UUID doctorId){
    List<DoctorSlots> slots = doctorSlotsRepository.findByDoctorIdAndStatus(doctorId, SlotStatus.AVAILABLE);
    return slots.stream().map(this::tDoctorSlotResponse).toList();
  }


  private DoctorSlotResponse tDoctorSlotResponse(DoctorSlots slot){
    return new DoctorSlotResponse(slot.getId(), slot.getDoctor().getId(), slot.getDoctor().getFirstName(), slot.getStartTime(), slot.getEndTime(), slot.getStatus(), slot.getReservedByAppointmentId());
  }

 public boolean doctorExists(UUID doctorId) {
  return repository.existsById(doctorId);
 }


 public List<DoctorResponse> getAllDoctors() {
  List<Doctor> doctors = repository.findAll();
  return doctors.stream().map(this::tDoctorResponse).toList();
 }

 public DoctorSlotResponse reserveSlot(UUID slotId, UUID appointmentId){
  DoctorSlots slot = doctorSlotsRepository.findById(slotId).orElseThrow(() -> new SlotNotFoundException("Slot not found with ID: " + slotId));
  if (slot.getStatus() != SlotStatus.AVAILABLE) {
   throw new IllegalArgumentException("Slot is not available for reservation");
  }
 slot.setStatus(SlotStatus.RESERVED);
slot.setReservedByAppointmentId(appointmentId);
doctorSlotsRepository.save(slot);
return tDoctorSlotResponse(slot);
 }

 public DoctorSlotResponse confirmSlot(UUID slotId)
 {
  DoctorSlots slot = doctorSlotsRepository.findById(slotId).orElseThrow(() -> new SlotNotFoundException("Slot not found with ID: " + slotId));
  if (slot.getStatus() != SlotStatus.RESERVED) {
   throw new IllegalArgumentException("Slot is not reserved and cannot be confirmed");
  }
  slot.setStatus(SlotStatus.CONFIRMED);
  doctorSlotsRepository.save(slot);
  return tDoctorSlotResponse(slot);
 }


public DoctorSlotResponse releaseSlot(UUID slotId)
{
  DoctorSlots slot = doctorSlotsRepository.findById(slotId).orElseThrow(() -> new SlotNotFoundException("Slot not found with ID: " + slotId));
  if (slot.getStatus() == SlotStatus.AVAILABLE) {
   throw new IllegalArgumentException("Slot is already available");
  }
  if(slot.getStatus() == SlotStatus.RESERVED || slot.getStatus() == SlotStatus.CONFIRMED){
    slot.setStatus(SlotStatus.AVAILABLE);
    slot.setReservedByAppointmentId(null);
    } 
  doctorSlotsRepository.save(slot);
  return tDoctorSlotResponse(slot);
}




 private DoctorResponse tDoctorResponse(Doctor savedDoctor) {
  DoctorResponse response = new DoctorResponse(savedDoctor.getId(), savedDoctor.getFirstName(),
    savedDoctor.getLastName(), savedDoctor.getSpecialization(), savedDoctor.getEmail(), savedDoctor.getPhoneNumber(),
    savedDoctor.getActive(), savedDoctor.getCreatedAt());

  return response;

 }
}

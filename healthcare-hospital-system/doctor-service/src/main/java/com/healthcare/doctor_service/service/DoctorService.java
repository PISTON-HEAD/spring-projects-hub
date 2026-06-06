package com.healthcare.doctor_service.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 @CacheEvict(value = "doctors", allEntries = true)
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
 @Cacheable(key = "#doctorId", value="doctor")
 public DoctorResponse getDoctorById(UUID doctorId) {
  Doctor doctor = repository.findById(doctorId)
    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

  return tDoctorResponse(doctor);
 }

 @Transactional
 @Caching(evict = {
    @CacheEvict(value = "availableSlots", allEntries = true),
    @CacheEvict(value = "slots", allEntries = true)
})
 public DoctorSlotResponse createDoctorSlot(UUID doctorId, CreateDoctorSlotRequest request) {
  Doctor doctor = repository.findById(doctorId)
    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

  if (doctorSlotsRepository.existsByDoctorIdAndStartTimeAndEndTime(doctorId, request.startTime(), request.endTime())) {
   throw new IllegalArgumentException("A slot already exists for this doctor at the given time");
  }

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
  @Cacheable(value = "slots", key = "#doctorId + '-' + #page + '-' + #size")
  public Page<DoctorSlotResponse> getSlotsByDoctor(UUID doctorId, int page, int size){
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
    Page<DoctorSlots> slots = doctorSlotsRepository.findByDoctorId(doctorId, pageable);
    return slots.map(this::tDoctorSlotResponse); 
  }

  @Transactional
  @Cacheable(value = "availableSlots", key = "#doctorId + '-' + #page + '-' + #size")
  public Page<DoctorSlotResponse> getAvailableSlots(UUID doctorId, int page, int size){
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
    Page<DoctorSlots> slots = doctorSlotsRepository.findByDoctorIdAndStatus(doctorId, SlotStatus.AVAILABLE, pageable);
    return slots.map(this::tDoctorSlotResponse);
  }


  private DoctorSlotResponse tDoctorSlotResponse(DoctorSlots slot){
    return new DoctorSlotResponse(slot.getId(), slot.getDoctor().getId(), slot.getDoctor().getFirstName(), slot.getStartTime(), slot.getEndTime(), slot.getStatus(), slot.getReservedByAppointmentId());
  }

 public boolean doctorExists(UUID doctorId) {
  return repository.existsById(doctorId);
 }


 @Transactional
 @Cacheable(key = "#page + '-' + #size", value = "doctors")
 public Page<DoctorResponse> getAllDoctors(int page, int size) {
  Pageable pageable = PageRequest.of(page,size, Sort.by("firstName").ascending());
  Page<Doctor> doctors = repository.findAll(pageable);
  return doctors.map(this::tDoctorResponse);
 }

 @Transactional
 @Caching(evict = {
    @CacheEvict(value = "availableSlots", allEntries = true),
    @CacheEvict(value = "slots", allEntries = true)
})
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

@Transactional
@Caching(evict = {
    @CacheEvict(value = "availableSlots", allEntries = true),
    @CacheEvict(value = "slots", allEntries = true)
})
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

@Transactional
@Caching(evict = {
    @CacheEvict(value = "availableSlots", allEntries = true),
    @CacheEvict(value = "slots", allEntries = true)
})
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

Ran terminal command: git checkout -b "feature/adding_unit_testcases"

Done. Now on branch `feature/adding_unit_testcases_doctor_&_patient_service`.

Ready to start unit tests. Before we dive in, here's what you need to know conceptually:

---

## 🧪 Unit Test Structure You'll Build

### Two types of tests per service:

**1. Service Tests** — pure logic, no Spring context, no DB
```
@ExtendWith(MockitoExtension.class)   ← lightweight, just Mockito
@Mock PatientRepository               ← fake repository
@InjectMocks PatientService           ← real service with mocks injected
```

**2. Controller Tests** — HTTP layer only, no real service
```
@WebMvcTest(PatientController.class)  ← loads only web layer
@MockBean PatientService              ← fake service (Spring context aware)
MockMvc mockMvc                       ← simulate HTTP requests
```

---

### Test cases to cover per service:

**PatientService:**
- `createPatient` → happy path returns response
- `createPatient` → duplicate email throws `IllegalArgumentException`
- `getPatientById` → found returns response
- `getPatientById` → not found throws `PatientNotFoundException`
- `existsById` → returns true/false
- `updatePatient` → partial update works

**DoctorService:**
- `createDoctor` → happy path
- `createDoctor` → duplicate email throws
- `getDoctorById` → found / not found
- `createDoctorSlot` → doctor exists, slot created
- `reserveSlot` → happy path (AVAILABLE → RESERVED)
- `reserveSlot` → already reserved throws
- `releaseSlot` → happy path (RESERVED → AVAILABLE)
- `confirmSlot` → happy path (RESERVED → CONFIRMED)
- `confirmSlot` → not reserved throws

---

Which do you want to start with — **PatientServiceTest** or **DoctorServiceTest**?

---

## 🏗️ Test Folder Structure

```
patient-service/src/test/java/com/healthcare/patient_service/
├── service/
│   └── PatientServiceTest.java       ← pure Mockito, no Spring
└── controller/
    └── PatientControllerTest.java    ← MockMvc, web layer only

doctor-service/src/test/java/com/healthcare/doctor_service/
├── service/
│   └── DoctorServiceTest.java        ← pure Mockito, no Spring
└── controller/
    └── DoctorControllerTest.java     ← MockMvc (after controller is built)
```

---

# 🏥 3. Healthcare Appointment + Billing System

### 💡 Core Idea

Book appointment → doctor availability → payment → prescription

### 🧠 Why it's useful

* You already worked in healthcare domain (Philips) → leverage that
* Domain knowledge = interview advantage

### 🏗️ Services

* Appointment Service
* Doctor Service
* Billing Service
* Patient Service

### ⚙️ Saga Flow

* Book appointment → reserve slot → payment
* Payment fails → release slot

---

### 2. Saga feels forced

Saga only makes sense when:

* Multiple services have **independent state changes**
* Failures require **compensation**

Right now:
👉 Only "payment fail → release slot" → too basic

---

# ✅ How to make this project actually strong

You need to **upgrade the problem**, not just implement it.

---

# 💥 Improved Version (This is what you should build)

## 💡 Realistic Flow

1. Patient books appointment
2. Doctor slot gets **reserved**
3. Billing service creates **payment request**
4. Payment is processed (external simulation)
5. Appointment gets **confirmed**
6. Notification sent
7. Doctor consultation → prescription generated
8. Very importantly we will be using GRPC and spring open feign for interservice communication. Please remember this

---

# 🏗️ Services (Final Design)

### 1. Patient Service

* Stores patient data

### 2. Doctor Service

* Manages doctor schedules
* Handles slot reservation

### 3. Appointment Service (**Orchestrator**)

* Controls Saga flow
* Maintains appointment state

### 4. Billing Service

* Handles payment lifecycle

### 5. Notification Service

* Sends email/SMS (mock)

---

# 🔁 Proper Saga Flow (Orchestration)

### Happy Flow

```
Create Appointment (PENDING)
→ Reserve Slot (Doctor Service)
→ Initiate Payment (Billing Service)
→ Payment Success
→ Confirm Appointment
→ Send Notification
```

---

### Failure Scenarios (THIS is where your project becomes legit)

#### ❌ Case 1: Slot unavailable

→ Fail immediately

#### ❌ Case 2: Payment fails

→ Trigger compensation:

* Release doctor slot
* Mark appointment as CANCELLED

#### ❌ Case 3: Notification fails

→ Retry using Resilience4j (no rollback needed)

#### ❌ Case 4: Doctor cancels after booking

→ Refund payment (compensation)

👉 This is what interviewers want to hear.

---

# ⚙️ Where each tech actually fits (no BS mapping)

### Kafka

* Appointment → Billing (event)
* Billing → Appointment (payment result)
* Notification events

👉 **DO NOT use Kafka for everything**
Use it where async makes sense.

---

### OpenFeign

* Synchronous call:
  * Appointment → Doctor (check + reserve slot)

---

### WebClient

* Simulate external payment gateway
* Non-blocking call

---

### Resilience4j

* Retry → payment service
* Circuit breaker → doctor service
* Rate limiter (optional)

---

### API Gateway

* Single entry point
* Routing + auth (basic JWT if you want extra weight)

---

# 🧠 What will make this stand out in interviews

If you can explain:

* Why you chose **Orchestration Saga over Choreography**
* How you handle **idempotency in Kafka consumers**
* How you avoid **double booking (race condition)**
* How compensation works step-by-step

👉 You're no longer a "2-year dev" — you look like a solid backend engineer.

---

# ⚠️ Brutal truth

If you:

* Just build endpoints
* Add Kafka producer/consumer randomly
* Don't model failures

👉 This project is useless.

If you:

* Design flows properly
* Handle edge cases
* Explain trade-offs

👉 This becomes your **strongest resume project**

---

# 🧠 0. First Rule (Non-Negotiable)

👉 You are NOT building microservices first
👉 You are building **a distributed workflow step-by-step**

If Kafka, Saga, retries are added before basic flow works → you'll get lost.

---

# 🏗️ 1. Project Structure (Recommended)

👉 **Multi-module Maven project**

```
healthcare-system/
│
├── api-gateway/
├── appointment-service/
├── doctor-service/
├── billing-service/
├── patient-service/
├── notification-service/
├── common-lib/   ← DTOs, events, enums
└── docker-compose.yml
```

---

# 🧱 2. Phase-wise Implementation Plan

---

## ✅ PHASE 1 — Build Core Services (NO Kafka, NO Saga)

👉 Goal: Make system work **synchronously first**

### Step 1: Doctor Service — slots, reserve, release, prevent double booking
### Step 2: Appointment Service — create, call Doctor Service (Feign), update status
### Step 3: Billing Service (basic) — create payment API, return success/failure manually
### Step 4: Integrate Payment (SYNC) — Appointment → Billing → response

🚨 **Checkpoint 1**: Book appointment + handle payment fail + release slot must all work before moving on.

---

## 🔁 PHASE 2 — Introduce Saga (Orchestration)

### Step 5: Introduce Kafka + Zookeeper (Docker)
### Step 6: Change flow — Appointment → Kafka → Billing → Kafka → Appointment
### Step 7: Implement Saga States — PENDING → PAYMENT_PENDING → CONFIRMED / CANCELLED
### Step 8: Payment Consumer — listen to payment-success / payment-failed
### Step 9: Compensation Logic — payment fails → release slot → CANCELLED

🚨 **Checkpoint 2**: Async flow works + compensation works + no data inconsistency.

---

## ⚙️ PHASE 3 — Add Resilience

### Step 10: Resilience4j — Retry (3 attempts) + Circuit breaker on Doctor Service calls
### Step 11: Simulate failures — doctor service down, billing delay, Kafka delay

---

## 🔁 PHASE 4 — Idempotency & Reliability

### Step 12: Handle duplicate Kafka events — `processed_events` table, ignore duplicate eventId
### Step 13: Safe consumers — if event already processed → skip

---

## 🌐 PHASE 5 — API Gateway

### Step 14: Setup Gateway — route /appointments, /doctors, /payments
### Step 15 (Optional): Add JWT authentication

---

## 🐳 PHASE 6 — Dockerize Everything

### Step 16: Docker Compose — PostgreSQL, Kafka, Zookeeper, all services
### Step 17: `docker-compose up` → full system runs

---

## 📊 PHASE 7 — Observability (Optional but HIGH IMPACT)

### Step 18: Logs + correlationId (VERY important)
### Step 19: Zipkin (distributed tracing)

---

## 🧪 PHASE 8 — Testing Strategy

* Happy Flow — appointment success
* Payment Failure — slot released
* Duplicate Event — no double processing
* Service Down — retry works

---

# 🔥 Final Architecture Flow

```
Client → Gateway
→ Appointment Service
→ Doctor Service (Feign)
→ Kafka (payment-initiated)
→ Billing Service
→ Kafka (payment result)
→ Appointment Service
→ Doctor Service (confirm/release)
→ Notification Service (Kafka)
```

---

# ⏱️ Realistic Timeline

| Phase            | Time     |
| ---------------- | -------- |
| Core services    | 3–4 days |
| Kafka + Saga     | 3 days   |
| Resilience       | 2 days   |
| Gateway + Docker | 2 days   |
| Testing + polish | 2 days   |

👉 Total: ~10–14 days (if focused)

---

# ✅ Final Inter-Service Communication Decisions

## Decision: Replace OpenFeign with gRPC for Doctor Service calls

**Reason:** appointment-service already has gRPC infrastructure wired to doctor-service for `checkDoctorExists`. Adding slot operations to the same proto is cleaner than introducing a second protocol (OpenFeign) for the same service pair.

**Rule:**
- appointment ↔ patient → **gRPC** (checkPatientExists)
- appointment ↔ doctor → **gRPC** (checkDoctorExists + reserveSlot + confirmSlot + releaseSlot)
- appointment ↔ billing → **Kafka** (async — payment initiation and result)
- billing / appointment → notification → **Kafka**

## Updated Final Architecture

```
Client → API Gateway
    │
    └── Appointment Service (Saga Orchestrator)
            │
            ├── gRPC ──────────► Patient Service   (checkPatientExists)
            ├── gRPC ──────────► Doctor Service    (checkDoctorExists,
            │                                       reserveSlot,
            │                                       confirmSlot,
            │                                       releaseSlot)
            └── Kafka ──────────► Billing Service  (payment-initiated topic)
                                        │
                                        └── Kafka ──► Appointment Service (payment-result topic)
                                        └── Kafka ──► Notification Service (notify topic)
```

## Updated doctor.proto (both doctor-service and appointment-service)

```protobuf
service DoctorGrpcService {
    rpc CheckDoctorExists (DoctorRequest)      returns (DoctorResponse);
    rpc ReserveSlot       (ReserveSlotRequest) returns (SlotResponse);
    rpc ConfirmSlot       (SlotRequest)        returns (SlotResponse);
    rpc ReleaseSlot       (SlotRequest)        returns (SlotResponse);
}
```

---

# 👉 Next Steps (in order)

1. **Build billing-service** — Spring Boot project, MySQL, payment entity, basic REST API
2. **Extend doctor.proto** — add ReserveSlot, ConfirmSlot, ReleaseSlot RPCs to both doctor-service and appointment-service proto files
3. **Implement gRPC slot handlers** in doctor-service (`DoctorGrpcServiceImpl`)
4. **Wire appointment-service** to call slot RPCs via gRPC instead of REST
5. **Integrate appointment → billing** synchronously first (REST call), verify end-to-end flow
6. **Introduce Kafka** — convert billing integration to async, implement Saga states and compensation


Extend doctor.proto — add ReserveSlot, ConfirmSlot, ReleaseSlot RPCs
Implement those RPCs in DoctorGrpcServiceImpl (doctor-service)
Update appointment-service — call reserveSlot via gRPC right after creation, add PAYMENT_PENDING Saga state
Then build billing-service — so appointment has something to call
Then wire Kafka between them
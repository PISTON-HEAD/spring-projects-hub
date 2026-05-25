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

package com.fhir.api.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fhir.api.entity.Patient;
import com.fhir.api.entity.PatientIdentifier;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FhirConversionService {

    private final FhirContext fhirContext;
    private final IParser jsonParser;

    public FhirConversionService() {
        this.fhirContext = FhirContext.forR4();
        this.jsonParser = fhirContext.newJsonParser().setPrettyPrint(false);
    }

    public org.hl7.fhir.r4.model.Patient convertToFhirPatient(Patient patient) {
        org.hl7.fhir.r4.model.Patient fhirPatient = new org.hl7.fhir.r4.model.Patient();

        // Set ID
        fhirPatient.setId(patient.getId().toString());

        // Set Identifiers
        if (patient.getIdentifiers() != null && !patient.getIdentifiers().isEmpty()) {
            List<Identifier> identifiers = new ArrayList<>();
            for (PatientIdentifier pi : patient.getIdentifiers()) {
                Identifier identifier = new Identifier();
                identifier.setSystem(pi.getSystem() != null ? pi.getSystem() : "hospital-system");
                identifier.setValue(pi.getIdentifierValue());
                identifier.setType(createCodeableConcept(pi.getIdentifierType()));
                identifiers.add(identifier);
            }
            fhirPatient.setIdentifier(identifiers);
        }

        // Set Name
        HumanName name = new HumanName();
        if (patient.getLastName() != null) {
            name.setFamily(patient.getLastName());
        }
        List<StringType> givenNames = new ArrayList<>();
        if (patient.getFirstName() != null) {
            givenNames.add(new StringType(patient.getFirstName()));
        }
        if (patient.getMiddleName() != null) {
            givenNames.add(new StringType(patient.getMiddleName()));
        }
        if (!givenNames.isEmpty()) {
            name.setGiven(givenNames);
        }
        fhirPatient.addName(name);

        // Set Gender
        if (patient.getGender() != null) {
            switch (patient.getGender().toLowerCase()) {
                case "male":
                    fhirPatient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "female":
                    fhirPatient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "other":
                    fhirPatient.setGender(Enumerations.AdministrativeGender.OTHER);
                    break;
                default:
                    fhirPatient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        // Set Birth Date
        if (patient.getDateOfBirth() != null) {
            fhirPatient.setBirthDateElement(new DateType(patient.getDateOfBirth().toString()));
        }

        // Set Telecom
        List<ContactPoint> telecoms = new ArrayList<>();
        if (patient.getPhone() != null) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(patient.getPhone());
            telecoms.add(phone);
        }
        if (patient.getEmail() != null) {
            ContactPoint email = new ContactPoint();
            email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            email.setValue(patient.getEmail());
            telecoms.add(email);
        }
        if (!telecoms.isEmpty()) {
            fhirPatient.setTelecom(telecoms);
        }

        // Set Address
        if (patient.getAddressLine1() != null || patient.getCity() != null) {
            Address address = new Address();
            List<StringType> lines = new ArrayList<>();
            if (patient.getAddressLine1() != null) {
                lines.add(new StringType(patient.getAddressLine1()));
            }
            if (patient.getAddressLine2() != null) {
                lines.add(new StringType(patient.getAddressLine2()));
            }
            if (!lines.isEmpty()) {
                address.setLine(lines);
            }
            if (patient.getCity() != null) {
                address.setCity(patient.getCity());
            }
            if (patient.getState() != null) {
                address.setState(patient.getState());
            }
            if (patient.getPostalCode() != null) {
                address.setPostalCode(patient.getPostalCode());
            }
            if (patient.getCountry() != null) {
                address.setCountry(patient.getCountry());
            }
            fhirPatient.addAddress(address);
        }

        // Set Active
        if (patient.getActive() != null) {
            fhirPatient.setActive(patient.getActive());
        }

        return fhirPatient;
    }

    public Bundle createBundle(List<Patient> patients) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(patients.size());

        List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
        for (Patient patient : patients) {
            org.hl7.fhir.r4.model.Patient fhirPatient = convertToFhirPatient(patient);
            Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
            entry.setFullUrl("Patient/" + patient.getId());
            entry.setResource(fhirPatient);
            entries.add(entry);
        }
        bundle.setEntry(entries);

        return bundle;
    }

    public String toJson(org.hl7.fhir.r4.model.Patient patient) {
        return jsonParser.encodeResourceToString(patient);
    }

    public String toJson(Bundle bundle) {
        return jsonParser.encodeResourceToString(bundle);
    }

    private CodeableConcept createCodeableConcept(String code) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(code);
        coding.setDisplay(code);
        codeableConcept.addCoding(coding);
        codeableConcept.setText(code);
        return codeableConcept;
    }
}

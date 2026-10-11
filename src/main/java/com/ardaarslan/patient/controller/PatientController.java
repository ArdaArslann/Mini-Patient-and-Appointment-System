package com.ardaarslan.patient.controller;

import com.ardaarslan.patient.model.Patient;
import com.ardaarslan.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173") // React'in default portuna izin veriyoruz
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    // Get all patients (GET /api/patients)
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // Search patients (GET /api/patients/search?keyword=...)
    @GetMapping("/search")
    public List<Patient> searchPatients(@RequestParam String keyword) {
        return patientRepository.search(keyword);
    }

    // Create patient (POST /api/patients)
    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        return patientRepository.save(patient);
    }

    // Update patient (PUT /api/patients/{id})
    @PutMapping("/{id}")
    public Patient updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        // Look up the existing patient
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı: " + id));

        // Copy the incoming fields onto it
        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setPhoneNumber(patientDetails.getPhoneNumber());
        patient.setAddress(patientDetails.getAddress());

        // Save the updated patient
        return patientRepository.save(patient);
    }

    // Delete patient (DELETE /api/patients/{id})
    @DeleteMapping("/{id}")
    public String deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
        return "Hasta başarıyla silindi.";
    }
}
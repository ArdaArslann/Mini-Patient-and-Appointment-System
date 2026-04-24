package org.example.openemr_project.controller;

import org.example.openemr_project.model.Patient;
import org.example.openemr_project.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173") // React'in default portuna izin veriyoruz
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    // Tüm hastaları getir (GET /api/patients)
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // Yeni hasta ekle (POST /api/patients)
    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        return patientRepository.save(patient);
    }

    // Hasta Güncelle (PUT /api/patients/{id})
    @PutMapping("/{id}")
    public Patient updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        // Önce hastayı veritabanında buluyoruz
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı: " + id));

        // Yeni gelen verileri mevcut hastaya set ediyoruz
        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setPhoneNumber(patientDetails.getPhoneNumber());
        patient.setAddress(patientDetails.getAddress());

        // Güncellenmiş haliyle kaydediyoruz
        return patientRepository.save(patient);
    }

    // Hasta Sil (DELETE /api/patients/{id})
    @DeleteMapping("/{id}")
    public String deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
        return "Hasta başarıyla silindi.";
    }
}
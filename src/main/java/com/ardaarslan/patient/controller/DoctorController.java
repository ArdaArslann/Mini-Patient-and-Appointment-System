package com.ardaarslan.patient.controller;

import com.ardaarslan.patient.model.Doctor;
import com.ardaarslan.patient.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    // Get all doctors
    @GetMapping
    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    // Get doctors by polyclinic
    @GetMapping("/by-polyclinic/{polyclinicId}")
    public List<Doctor> getByPolyclinic(@PathVariable Long polyclinicId) {
        return doctorRepository.findByPolyclinicId(polyclinicId);
    }

    // Search doctors
    @GetMapping("/search")
    public List<Doctor> search(@RequestParam String keyword) {
        return doctorRepository.search(keyword);
    }

    // Create doctor
    @PostMapping
    public Doctor create(@RequestBody Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // Update doctor
    @PutMapping("/{id}")
    public Doctor update(@PathVariable Long id, @RequestBody Doctor details) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı: " + id));
        doctor.setFirstName(details.getFirstName());
        doctor.setLastName(details.getLastName());
        doctor.setTitle(details.getTitle());
        doctor.setPolyclinicId(details.getPolyclinicId());
        return doctorRepository.save(doctor);
    }

    // Delete doctor
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        doctorRepository.deleteById(id);
        return "Doktor başarıyla silindi.";
    }
}

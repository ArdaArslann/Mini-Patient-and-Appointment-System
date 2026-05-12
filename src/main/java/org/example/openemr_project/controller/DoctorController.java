package org.example.openemr_project.controller;

import org.example.openemr_project.model.Doctor;
import org.example.openemr_project.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    // Tüm doktorları getir
    @GetMapping
    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    // Poliklinik bazlı doktorları getir
    @GetMapping("/by-polyclinic/{polyclinicId}")
    public List<Doctor> getByPolyclinic(@PathVariable Long polyclinicId) {
        return doctorRepository.findByPolyclinicId(polyclinicId);
    }

    // Doktor ara
    @GetMapping("/search")
    public List<Doctor> search(@RequestParam String keyword) {
        return doctorRepository.search(keyword);
    }

    // Yeni doktor ekle
    @PostMapping
    public Doctor create(@RequestBody Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    // Doktor güncelle
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

    // Doktor sil
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        doctorRepository.deleteById(id);
        return "Doktor başarıyla silindi.";
    }
}

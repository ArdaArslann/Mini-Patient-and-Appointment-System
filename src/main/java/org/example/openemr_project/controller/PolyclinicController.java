package org.example.openemr_project.controller;

import org.example.openemr_project.model.Polyclinic;
import org.example.openemr_project.repository.PolyclinicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polyclinics")
@CrossOrigin(origins = "http://localhost:5173")
public class PolyclinicController {

    @Autowired
    private PolyclinicRepository polyclinicRepository;

    // Tüm poliklinikleri getir
    @GetMapping
    public List<Polyclinic> getAll() {
        return polyclinicRepository.findAll();
    }

    // Poliklinik ara
    @GetMapping("/search")
    public List<Polyclinic> search(@RequestParam String keyword) {
        return polyclinicRepository.search(keyword);
    }

    // Yeni poliklinik ekle
    @PostMapping
    public Polyclinic create(@RequestBody Polyclinic polyclinic) {
        return polyclinicRepository.save(polyclinic);
    }

    // Poliklinik güncelle
    @PutMapping("/{id}")
    public Polyclinic update(@PathVariable Long id, @RequestBody Polyclinic details) {
        Polyclinic polyclinic = polyclinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poliklinik bulunamadı: " + id));
        polyclinic.setName(details.getName());
        polyclinic.setDescription(details.getDescription());
        return polyclinicRepository.save(polyclinic);
    }

    // Poliklinik sil
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        polyclinicRepository.deleteById(id);
        return "Poliklinik başarıyla silindi.";
    }
}

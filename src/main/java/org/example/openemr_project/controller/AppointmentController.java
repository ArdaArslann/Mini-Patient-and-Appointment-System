package org.example.openemr_project.controller;

import org.example.openemr_project.model.Appointment;
import org.example.openemr_project.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    // Randevu ara
    @GetMapping("/search")
    public List<Appointment> search(@RequestParam String keyword) {
        return appointmentRepository.search(keyword);
    }

    @PostMapping
    public Appointment create(@RequestBody Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @DeleteMapping("/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
        return "Randevu başarıyla silindi.";
    }

    // Randevu Güncelle (PUT /api/appointments/{id})
    @PutMapping("/{id}")
    public Appointment updateAppointment(@PathVariable Long id, @RequestBody Appointment details) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Randevu bulunamadı: " + id));

        appt.setPatientId(details.getPatientId());
        appt.setDoctorId(details.getDoctorId());
        appt.setAppointmentDate(details.getAppointmentDate());
        appt.setReason(details.getReason());

        return appointmentRepository.save(appt);
    }
}
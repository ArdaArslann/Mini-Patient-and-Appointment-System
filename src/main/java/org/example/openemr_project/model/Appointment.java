package org.example.openemr_project.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    private Long patientId; // Hasta ile ilişki kurulan ID [cite: 20]

    @Column(name = "appointment_date")
    private LocalDateTime appointmentDate; // Tarih ve saat bilgisi

    private String reason;
}
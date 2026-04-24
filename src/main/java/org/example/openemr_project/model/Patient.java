package org.example.openemr_project.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "patient_data")
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
    private Long id;

    @Column(name = "fname")
    private String firstName;

    @Column(name = "lname")
    private String lastName;

    @Column(name = "DOB")
    private LocalDate dateOfBirth;

    @Column(name = "phone_cell")
    private String phoneNumber;

    @Column(name = "address")
    private String address;
}
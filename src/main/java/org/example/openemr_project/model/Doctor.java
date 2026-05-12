package org.example.openemr_project.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_doctors")
@Data
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "title")
    private String title;

    @Column(name = "polyclinic_id")
    private Long polyclinicId;
}

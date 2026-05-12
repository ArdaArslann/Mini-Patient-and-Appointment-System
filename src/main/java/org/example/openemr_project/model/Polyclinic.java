package org.example.openemr_project.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_polyclinics")
@Data
public class Polyclinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
}

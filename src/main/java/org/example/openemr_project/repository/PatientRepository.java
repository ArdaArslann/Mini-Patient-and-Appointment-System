package org.example.openemr_project.repository;

import org.example.openemr_project.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Patient> search(@Param("keyword") String keyword);
}

package org.example.openemr_project.repository;

import org.example.openemr_project.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE LOWER(a.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointment> search(@Param("keyword") String keyword);
}
package org.example.openemr_project.repository;

import org.example.openemr_project.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByPolyclinicId(Long polyclinicId);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Doctor> search(@Param("keyword") String keyword);
}

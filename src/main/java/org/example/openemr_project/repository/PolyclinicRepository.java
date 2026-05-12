package org.example.openemr_project.repository;

import org.example.openemr_project.model.Polyclinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolyclinicRepository extends JpaRepository<Polyclinic, Long> {

    @Query("SELECT p FROM Polyclinic p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Polyclinic> search(@Param("keyword") String keyword);
}

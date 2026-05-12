package org.example.openemr_project.dto;

public record PatientWithAppointmentRequest(
        String fname,
        String lname,
        String sex,
        String dob,
        String date,
        String startTime,
        String endTime
) {
}

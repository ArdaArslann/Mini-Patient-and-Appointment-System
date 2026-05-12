package org.example.openemr_project.dto;

public record PatientWithAppointmentResponse(
        boolean success,
        String patientId
) {
}

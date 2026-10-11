package com.ardaarslan.patient.dto;

public record PatientWithAppointmentResponse(
        boolean success,
        String patientId
) {
}

package com.ardaarslan.patient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenEmrPatientRequest(
        String fname,
        String lname,
        String sex,
        @JsonProperty("DOB") String dob
) {
}

package org.example.openemr_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenEmrPatientRequest(
        String fname,
        String lname,
        String sex,
        @JsonProperty("DOB") String dob
) {
}

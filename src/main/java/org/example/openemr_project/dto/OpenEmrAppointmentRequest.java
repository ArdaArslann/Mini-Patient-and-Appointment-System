package org.example.openemr_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenEmrAppointmentRequest(
        @JsonProperty("pc_catid") String pcCatid,
        @JsonProperty("pc_title") String pcTitle,
        @JsonProperty("pc_duration") String pcDuration,
        @JsonProperty("pc_hometext") String pcHometext,
        @JsonProperty("pc_apptstatus") String pcApptStatus,
        @JsonProperty("pc_eventDate") String pcEventDate,
        @JsonProperty("pc_startTime") String pcStartTime,
        @JsonProperty("pc_facility") String pcFacility,
        @JsonProperty("pc_billing_location") String pcBillingLocation,
        @JsonProperty("pc_aid") String pcAid
) {
}

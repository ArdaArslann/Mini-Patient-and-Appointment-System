package com.ardaarslan.patient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenEmrTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}

package org.example.openemr_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenEmrTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}

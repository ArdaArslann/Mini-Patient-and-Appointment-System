package com.ardaarslan.patient.service;

import com.ardaarslan.patient.config.OpenEmrProperties;
import com.ardaarslan.patient.dto.OpenEmrPatientRequest;
import com.ardaarslan.patient.dto.PatientWithAppointmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenEmrServiceTest {

    private static final String BASE_URL = "http://openemr.test";
    private static final String TOKEN_URL = BASE_URL + "/oauth2/default/token";
    private static final String PATIENT_URL = BASE_URL + "/apis/default/api/patient";
    private static final String TOKEN_RESPONSE =
            "{\"access_token\":\"test-token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}";

    private MockRestServiceServer server;
    private OpenEmrService service;

    @BeforeEach
    void setUp() {
        OpenEmrProperties properties = new OpenEmrProperties();
        properties.setBaseUrl(BASE_URL + "/");
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");

        MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
        service = new OpenEmrService(new RestTemplateBuilder(customizer), properties, new ObjectMapper());
        server = customizer.getServer();
    }

    @Test
    void createPatientWithAppointment_createsPatientThenBooksAppointmentForReturnedPid() {
        server.expect(once(), requestTo(TOKEN_URL))
                .andExpect(method(POST))
                .andExpect(content().formDataContains(Map.of(
                        "grant_type", "password",
                        "client_id", "client-id",
                        "client_secret", "client-secret")))
                .andRespond(withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON));

        server.expect(once(), requestTo(PATIENT_URL))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(jsonPath("$.fname").value("Ayse"))
                .andExpect(jsonPath("$.sex").value("Female"))
                .andExpect(jsonPath("$.DOB").value("1990-04-12"))
                .andRespond(withSuccess("{\"data\":{\"pid\":42}}", MediaType.APPLICATION_JSON));

        server.expect(once(), requestTo(PATIENT_URL + "/42/appointment"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(jsonPath("$.pc_eventDate").value("2026-11-03"))
                .andExpect(jsonPath("$.pc_startTime").value("09:00"))
                .andExpect(jsonPath("$.pc_duration").value("2700"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        String patientId = service.createPatientWithAppointment(new PatientWithAppointmentRequest(
                "Ayse", "Yilmaz", "F", "1990-04-12", "2026-11-03", "09:00", "09:45"));

        assertThat(patientId).isEqualTo("42");
        server.verify();
    }

    @Test
    void getAccessToken_reusesCachedTokenUntilItExpires() {
        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON));

        String first = service.getAccessToken();
        String second = service.getAccessToken();

        assertThat(first).isEqualTo("test-token");
        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    void createAppointment_fallsBackTo30MinutesWhenEndTimeIsNotAfterStartTime() {
        server.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(PATIENT_URL))
                .andRespond(withSuccess("{\"pid\":\"7\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(PATIENT_URL + "/7/appointment"))
                .andExpect(jsonPath("$.pc_duration").value("1800"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service.createPatientWithAppointment(new PatientWithAppointmentRequest(
                "Mehmet", "Kaya", "M", "1985-01-01", "2026-11-03", "10:00", "09:00"));

        server.verify();
    }

    @Test
    void createPatient_throwsWhenResponseHasNoPatientId() {
        server.expect(once(), requestTo(PATIENT_URL))
                .andRespond(withSuccess("{\"validationErrors\":[]}", MediaType.APPLICATION_JSON));

        OpenEmrPatientRequest request = new OpenEmrPatientRequest("Ali", "Demir", "Male", "2000-01-01");

        assertThatThrownBy(() -> service.createPatient("test-token", request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pid");
        server.verify();
    }
}

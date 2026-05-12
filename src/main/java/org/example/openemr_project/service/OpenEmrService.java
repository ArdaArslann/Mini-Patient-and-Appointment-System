package org.example.openemr_project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.openemr_project.config.OpenEmrProperties;
import org.example.openemr_project.dto.OpenEmrAppointmentRequest;
import org.example.openemr_project.dto.OpenEmrPatientRequest;
import org.example.openemr_project.dto.OpenEmrTokenResponse;
import org.example.openemr_project.dto.PatientWithAppointmentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class OpenEmrService {

    private static final Logger log = LoggerFactory.getLogger(OpenEmrService.class);

    private static final String TOKEN_PATH = "/oauth2/default/token";
    private static final String PATIENT_PATH = "/apis/default/api/patient";
    private static final String APPOINTMENT_PATH_TEMPLATE = "/apis/default/api/patient/%s/appointment";

    private final RestTemplate restTemplate;
    private final OpenEmrProperties properties;
    private final ObjectMapper objectMapper;

    private final Object tokenLock = new Object();
    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public OpenEmrService(RestTemplateBuilder restTemplateBuilder,
                          OpenEmrProperties properties,
                          ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String createPatientWithAppointment(PatientWithAppointmentRequest request) {
        String token = getAccessToken();

        OpenEmrPatientRequest patientRequest = new OpenEmrPatientRequest(
                request.fname(),
                request.lname(),
                normalizeSex(request.sex()),
                request.dob()
        );

        String patientId = createPatient(token, patientRequest);

        OpenEmrAppointmentRequest appointmentRequest = new OpenEmrAppointmentRequest(
            properties.getAppointmentCategoryId(),
            properties.getAppointmentTitle(),
            calculateDurationSeconds(request.startTime(), request.endTime()),
            properties.getAppointmentTitle(),
            properties.getAppointmentStatus(),
                request.date(),
                request.startTime(),
            properties.getAppointmentFacilityId(),
            properties.getAppointmentBillingLocationId(),
            properties.getAppointmentProviderId()
        );

        createAppointment(token, patientId, appointmentRequest);
        return patientId;
    }

    public String getAccessToken() {
        Instant now = Instant.now();
        if (cachedAccessToken != null && now.isBefore(tokenExpiresAt.minusSeconds(30))) {
            return cachedAccessToken;
        }

        synchronized (tokenLock) {
            now = Instant.now();
            if (cachedAccessToken != null && now.isBefore(tokenExpiresAt.minusSeconds(30))) {
                return cachedAccessToken;
            }

            OpenEmrTokenResponse tokenResponse = fetchNewToken();
            long expiresIn = tokenResponse.expiresIn() != null ? tokenResponse.expiresIn() : 300L;

            this.cachedAccessToken = tokenResponse.accessToken();
            this.tokenExpiresAt = Instant.now().plusSeconds(expiresIn);

            log.info("OpenEMR token refreshed. Expires in {} seconds", expiresIn);
            return this.cachedAccessToken;
        }
    }

    public String createPatient(String token, OpenEmrPatientRequest patientRequest) {
        JsonNode response = postJson(PATIENT_PATH, token, patientRequest, "createPatient");

        String patientId = extractPatientId(response);
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalStateException("OpenEMR patient response does not contain pid");
        }

        return patientId;
    }

    public void createAppointment(String token, String patientId, OpenEmrAppointmentRequest appointmentRequest) {
        String appointmentPath = String.format(APPOINTMENT_PATH_TEMPLATE, patientId);
        postJson(appointmentPath, token, appointmentRequest, "createAppointment");
    }

    private OpenEmrTokenResponse fetchNewToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("username", properties.getUsername());
        form.add("password", properties.getPassword());
        form.add("user_role", properties.getUserRole());
        form.add("scope", properties.getScope());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);
        String url = buildUrl(TOKEN_PATH);

        log.info("OpenEMR token request -> POST {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("OpenEMR token response <- status={} body={}", response.getStatusCode().value(), response.getBody());
            return objectMapper.readValue(response.getBody(), OpenEmrTokenResponse.class);
        } catch (RestClientResponseException ex) {
            log.error("OpenEMR token request failed: status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
            throw new IllegalStateException("Failed to fetch OpenEMR token", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse OpenEMR token response", ex);
        }
    }

    private JsonNode postJson(String path, String token, Object body, String operationName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        String url = buildUrl(path);

        log.info("OpenEMR {} request -> POST {} body={}", operationName, url, toLogJson(body));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("OpenEMR {} response <- status={} body={}", operationName, response.getStatusCode().value(), response.getBody());
            return objectMapper.readTree(response.getBody());
        } catch (RestClientResponseException ex) {
            log.error("OpenEMR {} failed: status={} body={}", operationName, ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
            throw new IllegalStateException("OpenEMR " + operationName + " failed", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse OpenEMR response for " + operationName, ex);
        }
    }

    private String extractPatientId(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }

        if (response.hasNonNull("pid")) {
            return response.get("pid").asText();
        }
        if (response.has("data") && response.get("data").hasNonNull("pid")) {
            return response.get("data").get("pid").asText();
        }
        if (response.has("result") && response.get("result").hasNonNull("pid")) {
            return response.get("result").get("pid").asText();
        }
        if (response.hasNonNull("id")) {
            return response.get("id").asText();
        }

        return null;
    }

    private String normalizeSex(String sex) {
        if (sex == null || sex.isBlank()) {
            return "Male";
        }

        String upper = sex.trim().toUpperCase();
        if ("M".equals(upper) || "MALE".equals(upper) || "ERKEK".equals(upper)) {
            return "Male";
        }
        if ("F".equals(upper) || "FEMALE".equals(upper) || "KADIN".equals(upper)) {
            return "Female";
        }

        return "Male";
    }

    private String calculateDurationSeconds(String startTime, String endTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);
            int seconds = (int) java.time.Duration.between(start, end).getSeconds();
            if (seconds <= 0) {
                return "1800";
            }
            return String.valueOf(seconds);
        } catch (DateTimeParseException | NullPointerException ex) {
            return "1800";
        }
    }

    private String buildUrl(String path) {
        String base = properties.getBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }

    private String toLogJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (IOException ex) {
            return String.valueOf(body);
        }
    }
}

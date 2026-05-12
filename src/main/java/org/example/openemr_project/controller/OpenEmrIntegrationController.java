package org.example.openemr_project.controller;

import org.example.openemr_project.dto.PatientWithAppointmentRequest;
import org.example.openemr_project.dto.PatientWithAppointmentResponse;
import org.example.openemr_project.service.OpenEmrService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class OpenEmrIntegrationController {

    private final OpenEmrService openEmrService;

    public OpenEmrIntegrationController(OpenEmrService openEmrService) {
        this.openEmrService = openEmrService;
    }

    @PostMapping("/patient-with-appointment")
    public PatientWithAppointmentResponse createPatientWithAppointment(@RequestBody PatientWithAppointmentRequest request) {
        try {
            String patientId = openEmrService.createPatientWithAppointment(request);
            return new PatientWithAppointmentResponse(true, patientId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenEMR integration failed: " + ex.getMessage(), ex);
        }
    }
}

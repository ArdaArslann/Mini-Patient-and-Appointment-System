package org.example.openemr_project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openemr")
public class OpenEmrProperties {

    private String baseUrl = "http://localhost:8080";
    private String clientId = "YOUR_CLIENT_ID";
    private String clientSecret = "YOUR_CLIENT_SECRET";
    private String username = "apiuser";
    private String password = "123456";
    private String userRole = "users";
    private String scope = "api:oemr user/patient.write user/appointment.write";
    private String appointmentCategoryId = "10";
    private String appointmentTitle = "Checkup";
    private String appointmentStatus = "-";
    private String appointmentFacilityId = "3";
    private String appointmentBillingLocationId = "3";
    private String appointmentProviderId = "1";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAppointmentCategoryId() {
        return appointmentCategoryId;
    }

    public void setAppointmentCategoryId(String appointmentCategoryId) {
        this.appointmentCategoryId = appointmentCategoryId;
    }

    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    public void setAppointmentTitle(String appointmentTitle) {
        this.appointmentTitle = appointmentTitle;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getAppointmentFacilityId() {
        return appointmentFacilityId;
    }

    public void setAppointmentFacilityId(String appointmentFacilityId) {
        this.appointmentFacilityId = appointmentFacilityId;
    }

    public String getAppointmentBillingLocationId() {
        return appointmentBillingLocationId;
    }

    public void setAppointmentBillingLocationId(String appointmentBillingLocationId) {
        this.appointmentBillingLocationId = appointmentBillingLocationId;
    }

    public String getAppointmentProviderId() {
        return appointmentProviderId;
    }

    public void setAppointmentProviderId(String appointmentProviderId) {
        this.appointmentProviderId = appointmentProviderId;
    }
}

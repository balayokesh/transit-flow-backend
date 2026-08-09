package com.transitflow.dto;

import java.time.Instant;

public class AlertResponse {

    private String alertId;
    private String actualRoute;
    private String location;
    private Instant reportedAt;

    public AlertResponse() {
    }

    public AlertResponse(String alertId, String actualRoute, String location, Instant reportedAt) {
        this.alertId = alertId;
        this.actualRoute = actualRoute;
        this.location = location;
        this.reportedAt = reportedAt;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getActualRoute() {
        return actualRoute;
    }

    public void setActualRoute(String actualRoute) {
        this.actualRoute = actualRoute;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }
}

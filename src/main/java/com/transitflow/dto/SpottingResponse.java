package com.transitflow.dto;

public class SpottingResponse {

    private String spottingId;
    private String detectedRoute;
    private String location;
    private boolean mismatch;
    private String alertId;

    public SpottingResponse() {
    }

    public SpottingResponse(String spottingId, String detectedRoute,
            String location, boolean mismatch, String alertId) {
        this.spottingId = spottingId;
        this.detectedRoute = detectedRoute;
        this.location = location;
        this.mismatch = mismatch;
        this.alertId = alertId;
    }

    public String getSpottingId() {
        return spottingId;
    }

    public void setSpottingId(String spottingId) {
        this.spottingId = spottingId;
    }

    public String getDetectedRoute() {
        return detectedRoute;
    }

    public void setDetectedRoute(String detectedRoute) {
        this.detectedRoute = detectedRoute;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isMismatch() {
        return mismatch;
    }

    public void setMismatch(boolean mismatch) {
        this.mismatch = mismatch;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }
}

package com.transitflow.dto;

import jakarta.validation.constraints.NotBlank;

public class SpottingRequest {

    @NotBlank(message = "routeNumber must not be blank")
    private String routeNumber;

    @NotBlank(message = "location must not be blank")
    private String location;

    private boolean mismatch;

    public SpottingRequest() {
    }

    public SpottingRequest(String routeNumber, String location, boolean mismatch) {
        this.routeNumber = routeNumber;
        this.location = location;
        this.mismatch = mismatch;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
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
}

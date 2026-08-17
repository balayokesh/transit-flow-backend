package com.transitflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class ErrorResponse {

    private String error;
    private String code;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(String error, String code) {
        this.error = error;
        this.code = code;
        this.timestamp = Instant.now();
    }

    public ErrorResponse(String error, String code, Instant timestamp) {
        this.error = error;
        this.code = code;
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

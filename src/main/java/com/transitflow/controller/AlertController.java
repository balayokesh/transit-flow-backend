package com.transitflow.controller;

import com.transitflow.dto.AlertResponse;
import com.transitflow.model.Alert;
import com.transitflow.service.AlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * GET /api/alerts
     * Returns all route-mismatch alerts stored in memory.
     */
    @GetMapping
    public List<AlertResponse> getAllAlerts() {
        return alertService.getAllAlerts().stream()
                .map(this::toResponse)
                .toList();
    }

    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getAlertId(),
                alert.getActualRoute(),
                alert.getLocation(),
                alert.getReportedAt());
    }
}

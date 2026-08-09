package com.transitflow.service;

import com.transitflow.dto.SpottingRequest;
import com.transitflow.dto.SpottingResponse;
import com.transitflow.model.Alert;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orchestrates the bus-sighting submission flow:
 * 1. Determine the expected route for the reported location.
 * 2. Compare it with the detected (spotted) route.
 * 3. If they differ, create an alert.
 * 4. Return a structured response.
 */
@Service
public class SpottingService {

    private final AlertService alertService;

    private final AtomicInteger spottingCounter = new AtomicInteger(1000);

    public SpottingService(RouteValidationService routeValidationService,
            AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Processes a bus sighting submission.
     *
     * @param request contains the detected routeNumber and location
     * @return SpottingResponse with mismatch status and optional alert details
     */
    public SpottingResponse processSpotting(SpottingRequest request) {
        String detectedRoute = request.getRouteNumber().trim();
        String location = request.getLocation().trim().toUpperCase();
        boolean mismatch = request.isMismatch();

        String spottingId = "SP-" + spottingCounter.incrementAndGet();
        String alertId = null;

        if (mismatch) {
            Alert alert = alertService.createAlert(detectedRoute, location);
            alertId = alert.getAlertId();
        }

        return new SpottingResponse(
                spottingId,
                detectedRoute,
                location,
                mismatch,
                alertId);
    }
}

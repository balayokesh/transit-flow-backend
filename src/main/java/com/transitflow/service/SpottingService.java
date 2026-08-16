package com.transitflow.service;

import com.transitflow.dto.SpottingRequest;
import com.transitflow.dto.SpottingResponse;
import com.transitflow.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SpottingService.class);

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
        log.info("Processing spotting ID '{}': detectedRoute='{}', location='{}', mismatch={}",
                spottingId, detectedRoute, location, mismatch);

        String alertId = null;

        if (mismatch) {
            log.warn("Spotting mismatch detected for spotting ID '{}' (route='{}', location='{}'). Creating alert.",
                    spottingId, detectedRoute, location);
            Alert alert = alertService.createAlert(detectedRoute, location);
            alertId = alert.getAlertId();
            log.info("Created alert '{}' for spotting ID '{}'", alertId, spottingId);
        } else {
            log.info("No mismatch for spotting ID '{}' (route='{}', location='{}')",
                    spottingId, detectedRoute, location);
        }

        return new SpottingResponse(
                spottingId,
                detectedRoute,
                location,
                mismatch,
                alertId);
    }
}

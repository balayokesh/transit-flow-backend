package com.transitflow.controller;

import com.transitflow.dto.SpottingRequest;
import com.transitflow.dto.SpottingResponse;
import com.transitflow.service.SpottingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spottings")
public class SpottingController {

    private static final Logger log = LoggerFactory.getLogger(SpottingController.class);

    private final SpottingService spottingService;

    public SpottingController(SpottingService spottingService) {
        this.spottingService = spottingService;
    }

    /**
     * POST /api/spottings
     * Submit a bus sighting. Returns mismatch status and alert info if applicable.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpottingResponse submitSpotting(@Valid @RequestBody SpottingRequest request) {
        log.info("Received spotting submission: routeNumber='{}', location='{}', mismatch={}",
                request.getRouteNumber(), request.getLocation(), request.isMismatch());
        SpottingResponse response = spottingService.processSpotting(request);
        log.info("Completed spotting submission: spottingId='{}', alertId='{}'",
                response.getSpottingId(), response.getAlertId());
        return response;
    }
}

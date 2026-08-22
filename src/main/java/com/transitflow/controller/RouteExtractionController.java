package com.transitflow.controller;

import com.transitflow.dto.RouteExtractionResponse;
import com.transitflow.dto.SpottingRequest;
import com.transitflow.service.GeminiRouteExtractionService;
import com.transitflow.service.RouteService;
import com.transitflow.service.SpottingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/api/gemini")
public class RouteExtractionController {

    private static final Logger log = LoggerFactory.getLogger(RouteExtractionController.class);

    private static final String HARDCODED_LOCATION = "UKKADAM";

    private final GeminiRouteExtractionService extractionService;
    private final SpottingService spottingService;
    private final Bucket bucket;

    public RouteExtractionController(GeminiRouteExtractionService extractionService,
            SpottingService spottingService,
            RouteService routeService) {
        this.extractionService = extractionService;
        this.spottingService = spottingService;

        // 10 requests per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping(value = "/extract-route", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractRoute(@RequestParam("image") MultipartFile image)
            throws IOException {

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for route extraction endpoint");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many requests. Please try again later.");
        }

        if (image == null || image.isEmpty()) {
            log.warn("Route extraction request rejected: image file is empty or null");
            throw new IllegalArgumentException("Image file must not be empty");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Route extraction request rejected: invalid content type '{}'", contentType);
            throw new IllegalArgumentException("Unsupported file type. Please upload an image.");
        }

        log.info("Starting Gemini OCR for image: filename='{}', size={} bytes",
                image.getOriginalFilename(), image.getSize());
        RouteExtractionResponse response = extractionService.extractRoute(image);
        log.info("Successfully completed Gemini OCR. Result: actual='{}', desired='{}', confidence='{}'",
                response.actualRoute(), response.desiredRoute(), response.confidence());

        String actualRoute = response.actualRoute();
        String desiredRoute = response.desiredRoute();

        // Branch 1: AI could not extract a route number from the image
        if (actualRoute == null) {
            log.info("actualRoute is null — no action needed.");
            return ResponseEntity.ok(response);
        }

        // Branch 2: Routes match — everything is fine
        if (actualRoute.equalsIgnoreCase(desiredRoute)) {
            log.info("actualRoute ('{}') matches desiredRoute — no action needed.", actualRoute);
            return ResponseEntity.ok(response);
        }

        // Branch 3: Route mismatch
        log.warn("Route mismatch detected: actual='{}', desired='{}'. Triggering spotting submission.",
                actualRoute, desiredRoute);
        spottingService.processSpotting(new SpottingRequest(desiredRoute, HARDCODED_LOCATION, true));

        return ResponseEntity.ok(response);
    }
}

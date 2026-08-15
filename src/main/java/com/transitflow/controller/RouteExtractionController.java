package com.transitflow.controller;

import com.transitflow.dto.RouteExtractionResponse;
import com.transitflow.dto.SpottingRequest;
import com.transitflow.service.GeminiRouteExtractionService;
import com.transitflow.service.RouteService;
import com.transitflow.service.SpottingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/gemini")
public class RouteExtractionController {

    private static final String HARDCODED_LOCATION = "UKKADAM";

    private final GeminiRouteExtractionService extractionService;
    private final SpottingService spottingService;

    public RouteExtractionController(GeminiRouteExtractionService extractionService,
            SpottingService spottingService,
            RouteService routeService) {
        this.extractionService = extractionService;
        this.spottingService = spottingService;
    }

    @PostMapping(value = "/extract-route", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractRoute(@RequestParam("image") MultipartFile image)
            throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Unsupported file type. Please upload an image.");
        }

        System.out.println("Starting gemini ocr for image: " + image.getOriginalFilename());
        RouteExtractionResponse response = extractionService.extractRoute(image);
        System.out.println("Successfully completed gemini ocr");

        String actualRoute = response.actualRoute();
        String desiredRoute = response.desiredRoute();

        // Branch 1: AI could not extract a route number from the image
        if (actualRoute == null) {
            System.out.println("actualRoute is null — no action needed.");
            return ResponseEntity.ok(response);
        }

        // Branch 2: Routes match — everything is fine
        if (actualRoute.equalsIgnoreCase(desiredRoute)) {
            System.out.println("actualRoute (" + actualRoute + ") matches desiredRoute — no action needed.");
            return ResponseEntity.ok(response);
        }

        // Branch 3: Route mismatch
        System.out.println("Route mismatch detected: actual=" + actualRoute
                + ", desired=" + desiredRoute);
        spottingService.processSpotting(new SpottingRequest(desiredRoute, HARDCODED_LOCATION, true));

        return ResponseEntity.ok(response);
    }
}

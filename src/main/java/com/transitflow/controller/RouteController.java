package com.transitflow.controller;

import com.transitflow.model.Route;
import com.transitflow.service.RouteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * GET /api/routes/{routeNumber}
     * Returns hardcoded route details for the given route number.
     */
    @GetMapping("/{routeNumber}")
    public Route getRoute(@PathVariable String routeNumber) {
        return routeService.getRoute(routeNumber);
    }
}

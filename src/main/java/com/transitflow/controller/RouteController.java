package com.transitflow.controller;

import com.transitflow.model.Route;
import com.transitflow.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

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
        log.info("Fetching route details for routeNumber='{}'", routeNumber);
        Route route = routeService.getRoute(routeNumber);
        log.info("Found route: routeNumber='{}', routeName='{}'", route.getRouteNumber(), route.getName());
        return route;
    }
}

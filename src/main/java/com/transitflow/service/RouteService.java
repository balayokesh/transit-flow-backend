package com.transitflow.service;

import com.transitflow.exception.RouteNotFoundException;
import com.transitflow.model.Route;
import com.transitflow.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides route information.
 */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Returns route details for the given route number.
     *
     * @param routeNumber the route identifier
     * @return Route information
     * @throws RouteNotFoundException if the route is not in the repository
     */
    public Route getRoute(String routeNumber) {
        String trimmed = routeNumber != null ? routeNumber.trim() : "";
        log.debug("Looking up route '{}' in repository", trimmed);
        Route route = routeRepository.findByRouteNumber(trimmed);
        if (route == null) {
            log.warn("Route '{}' was not found in repository", trimmed);
            throw new RouteNotFoundException(
                    "Route '" + routeNumber + "' is not found."
            );
        }
        return route;
    }

    /**
     * Returns all routes.
     *
     * @return a list of all Routes
     */
    public List<Route> getAllRoutes() {
        log.debug("Retrieving all routes from repository");
        return routeRepository.findAll();
    }
}


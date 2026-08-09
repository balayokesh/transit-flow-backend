package com.transitflow.service;

import com.transitflow.exception.RouteNotFoundException;
import com.transitflow.model.Route;
import com.transitflow.repository.RouteRepository;
import org.springframework.stereotype.Service;

/**
 * Provides route information.
 */
@Service
public class RouteService {

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
        Route route = routeRepository.findByRouteNumber(routeNumber.trim());
        if (route == null) {
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
    public java.util.List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }
}

package com.transitflow.repository;

import com.transitflow.model.Route;

/**
 * Repository interface for accessing Route data.
 */
public interface RouteRepository {

    /**
     * Finds a route by its number.
     *
     * @param routeNumber the route number to find
     * @return the Route if found, null otherwise
     */
    Route findByRouteNumber(String routeNumber);

    /**
     * Returns all routes.
     *
     * @return a list of all Routes
     */
    java.util.List<Route> findAll();
}

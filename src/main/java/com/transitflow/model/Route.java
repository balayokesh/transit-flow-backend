package com.transitflow.model;

import java.util.List;

public class Route {

    private String routeNumber;
    private String name;
    private List<String> stops;

    public Route() {}

    public Route(String routeNumber, String name, List<String> stops) {
        this.routeNumber = routeNumber;
        this.name = name;
        this.stops = stops;
    }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }
}

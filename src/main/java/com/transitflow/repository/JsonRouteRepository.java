package com.transitflow.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transitflow.model.Route;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Routes loaded from a JSON file.
 */
@Repository
public class JsonRouteRepository implements RouteRepository {

    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final String datasetPath;

    public JsonRouteRepository(ObjectMapper objectMapper, @Value("${dataset.path:data/dataset.json}") String datasetPath) {
        this.objectMapper = objectMapper;
        this.datasetPath = datasetPath;
    }

    @PostConstruct
    public void init() {
        try {
            final String datasetPath2 = datasetPath;
            if (datasetPath2 != null) {
                ClassPathResource resource = new ClassPathResource(datasetPath2);
                if (!resource.exists()) {
                    throw new IllegalStateException("Dataset not found on classpath: " + datasetPath);
                }
                try (InputStream is = resource.getInputStream()) {
                    List<Route> routeList = objectMapper.readValue(is, new TypeReference<List<Route>>() {});
                    for (Route route : routeList) {
                        routes.put(route.getRouteNumber(), route);
                    }
                }
            } else {
                System.out.println("Dataset not found");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load dataset during startup", e);
        }
    }

    @Override
    public Route findByRouteNumber(String routeNumber) {
        return routes.get(routeNumber);
    }

    @Override
    public List<Route> findAll() {
        return new java.util.ArrayList<>(routes.values());
    }
}

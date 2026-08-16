package com.transitflow.service;

import com.transitflow.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages in-memory storage of alerts.
 * Thread-safe — no database required in Sprint 1.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final CopyOnWriteArrayList<Alert> alerts = new CopyOnWriteArrayList<>();
    private final AtomicInteger alertCounter = new AtomicInteger(1000);

    /**
     * Creates and stores a new alert for a route mismatch.
     *
     * @param actualRoute the route the spotter observed
     * @param location    where the sighting occurred
     * @return the persisted Alert
     */
    public Alert createAlert(String actualRoute, String location) {
        String alertId = "ALT-" + alertCounter.incrementAndGet();
        Alert alert = new Alert(alertId, actualRoute, location, Instant.now());
        alerts.add(alert);
        log.info("Persisted alert: alertId='{}', actualRoute='{}', location='{}', totalAlerts={}",
                alertId, actualRoute, location, alerts.size());
        return alert;
    }

    /**
     * Returns an unmodifiable snapshot of all current alerts.
     */
    public List<Alert> getAllAlerts() {
        log.debug("Retrieving all alerts (count={})", alerts.size());
        return Collections.unmodifiableList(new ArrayList<>(alerts));
    }
}


package com.transitflow.dto;

public record RouteExtractionResponse(
                String desiredRoute,
                String actualRoute,
                String confidence) {
}

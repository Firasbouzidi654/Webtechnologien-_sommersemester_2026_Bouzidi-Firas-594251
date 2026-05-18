package de.htw_berlin.KinderCareConnect.rest.model;

import java.time.Instant;

public record WeatherHealthResponse(
    String city,
    String country,
    Double temperature,
    Double feelsLike,
    String condition,
    String description,
    String icon,
    Integer humidity,
    Double windSpeed,
    Integer cloudiness,
    Double rainVolume,
    Integer rainProbability,
    Instant observedAt
) {
}

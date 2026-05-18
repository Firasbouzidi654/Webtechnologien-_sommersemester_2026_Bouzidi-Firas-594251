package de.htw_berlin.KinderCareConnect.business.service;

import com.fasterxml.jackson.databind.JsonNode;
import de.htw_berlin.KinderCareConnect.rest.model.WeatherHealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
public class OpenWeatherService {

    private final RestClient restClient;
    private final String apiKey;

    public OpenWeatherService(@Value("${openweather.api.key:}") String apiKey) {
        this.restClient = RestClient.builder()
            .baseUrl("https://api.openweathermap.org/data/2.5")
            .build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public WeatherHealthResponse getHealthWeather(double lat, double lon) {
        if (apiKey.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "OpenWeather API key is not configured. Add OPENWEATHER_API_KEY in Render or local environment."
            );
        }

        validateCoordinates(lat, lon);

        try {
            JsonNode current = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/weather")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .build())
                .retrieve()
                .body(JsonNode.class);

            JsonNode forecast = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/forecast")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .build())
                .retrieve()
                .body(JsonNode.class);

            return normalizeWeather(current, forecast);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "OpenWeather data is temporarily unavailable.",
                exception
            );
        }
    }

    private void validateCoordinates(double lat, double lon) {
        if (Double.isNaN(lat) || Double.isNaN(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude or longitude is invalid.");
        }
    }

    private WeatherHealthResponse normalizeWeather(JsonNode current, JsonNode forecast) {
        JsonNode weather = current.path("weather").isArray() && current.path("weather").size() > 0
            ? current.path("weather").get(0)
            : null;

        return new WeatherHealthResponse(
            text(current.path("name"), "Local area"),
            text(current.path("sys").path("country"), ""),
            number(current.path("main").path("temp")),
            number(current.path("main").path("feels_like")),
            text(weather == null ? null : weather.path("main"), "Weather"),
            capitalize(text(weather == null ? null : weather.path("description"), "Current conditions")),
            text(weather == null ? null : weather.path("icon"), ""),
            integer(current.path("main").path("humidity")),
            number(current.path("wind").path("speed")),
            integer(current.path("clouds").path("all")),
            number(current.path("rain").path("1h")),
            rainProbability(forecast),
            observedAt(current.path("dt"))
        );
    }

    private String text(JsonNode node, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }

        String value = node.asText("").trim();
        return value.isBlank() ? fallback : value;
    }

    private Double number(JsonNode node) {
        return node != null && node.isNumber() ? node.asDouble() : null;
    }

    private Integer integer(JsonNode node) {
        return node != null && node.isNumber() ? node.asInt() : null;
    }

    private Integer rainProbability(JsonNode forecast) {
        JsonNode firstForecast = forecast.path("list").isArray() && forecast.path("list").size() > 0
            ? forecast.path("list").get(0)
            : null;

        if (firstForecast == null || !firstForecast.path("pop").isNumber()) {
            return null;
        }

        return (int) Math.round(firstForecast.path("pop").asDouble() * 100);
    }

    private Instant observedAt(JsonNode node) {
        return node != null && node.isNumber()
            ? Instant.ofEpochSecond(node.asLong())
            : Instant.now();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}

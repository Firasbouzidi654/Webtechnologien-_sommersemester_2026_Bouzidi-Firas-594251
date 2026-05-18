package de.htw_berlin.KinderCareConnect.rest.controller;

import de.htw_berlin.KinderCareConnect.business.service.OpenWeatherService;
import de.htw_berlin.KinderCareConnect.rest.model.WeatherHealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final OpenWeatherService openWeatherService;

    public WeatherController(OpenWeatherService openWeatherService) {
        this.openWeatherService = openWeatherService;
    }

    @GetMapping("/health")
    public WeatherHealthResponse getWeatherHealth(
        @RequestParam double lat,
        @RequestParam double lon
    ) {
        return openWeatherService.getHealthWeather(lat, lon);
    }
}

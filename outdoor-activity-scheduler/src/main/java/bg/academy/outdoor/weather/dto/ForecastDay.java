package bg.academy.outdoor.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ForecastDay(
        String date,
        Astro astro,
        List<HourWeather> hour
) {
}

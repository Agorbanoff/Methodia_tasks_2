package bg.academy.outdoor.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Astro(
        String sunrise,
        String sunset
) {
}

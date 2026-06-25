package bg.academy.outdoor.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherApiResponse(Forecast forecast) {
}

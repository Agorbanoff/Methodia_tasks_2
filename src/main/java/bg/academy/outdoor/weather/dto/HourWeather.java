package bg.academy.outdoor.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HourWeather(
        String time,
        double temp_c,
        int chance_of_rain,
        int cloud,
        double gust_kph
) {
}

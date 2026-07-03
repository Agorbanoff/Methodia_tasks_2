package bg.academy.outdoor.weather;

import bg.academy.outdoor.weather.dto.WeatherApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Objects;

@Component
public class WeatherClient {
    private static final String FORECAST_PATH = "/v1/forecast.json";

    private final RestClient restClient;

    public WeatherClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public WeatherApiResponse getForecast(String apiKey, String location, int days) {
        try {
            return Objects.requireNonNull(restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(FORECAST_PATH)
                            .queryParam("key", apiKey)
                            .queryParam("q", location)
                            .queryParam("days", days)
                            .queryParam("aqi", "no")
                            .queryParam("alerts", "no")
                            .build())
                    .retrieve()
                    .body(WeatherApiResponse.class));
        } catch (RestClientResponseException e) {
            throw new RuntimeException("WeatherAPI request failed with status "
                    + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to call WeatherAPI", e);
        }
    }
}

package bg.academy.outdoor.weather;

import bg.academy.outdoor.weather.dto.WeatherApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WeatherClient {
    private static final String FORECAST_URL =
            "https://api.weatherapi.com/v1/forecast.json?key=%s&q=%s&days=%d&aqi=no&alerts=no";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WeatherClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public WeatherApiResponse getForecast(String apiKey, String location, int days) {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = FORECAST_URL.formatted(apiKey, encodedLocation, days);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("WeatherAPI request failed with status "
                        + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), WeatherApiResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to call WeatherAPI", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WeatherAPI request was interrupted", e);
        }
    }
}

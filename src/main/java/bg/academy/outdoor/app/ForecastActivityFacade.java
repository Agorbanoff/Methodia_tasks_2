package bg.academy.outdoor.app;

import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.output.SportResult;
import bg.academy.outdoor.service.ActivityService;
import bg.academy.outdoor.weather.WeatherClient;
import bg.academy.outdoor.weather.dto.WeatherApiResponse;

import java.util.List;

public class ForecastActivityFacade {
    private final AppConfig config;
    private final WeatherClient weatherClient;
    private final ActivityService activityService;

    public ForecastActivityFacade(AppConfig config, WeatherClient weatherClient, ActivityService activityService) {
        this.config = config;
        this.weatherClient = weatherClient;
        this.activityService = activityService;
    }

    public List<SportResult> calculate() {
        String apiKey = System.getenv("WEATHER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Missing WEATHER_API_KEY environment variable.");
        }

        WeatherApiResponse weather = weatherClient.getForecast(
                apiKey,
                config.location(),
                config.daysAhead() + 1
        );

        return activityService.calculateResults(config, weather);
    }
}

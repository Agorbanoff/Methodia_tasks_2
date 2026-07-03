package bg.academy.outdoor.excel;

import bg.academy.outdoor.config.OutdoorActivityProperties;
import bg.academy.outdoor.output.SportResult;
import bg.academy.outdoor.service.ActivityService;
import bg.academy.outdoor.weather.WeatherClient;
import bg.academy.outdoor.weather.dto.WeatherApiResponse;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class ExcelActivityService {
    private final OutdoorActivityProperties properties;
    private final ExcelInputService excelInputService;
    private final ExcelOutputService excelOutputService;
    private final WeatherClient weatherClient;
    private final ActivityService activityService;

    public ExcelActivityService(
            OutdoorActivityProperties properties,
            ExcelInputService excelInputService,
            ExcelOutputService excelOutputService,
            WeatherClient weatherClient,
            ActivityService activityService
    ) {
        this.properties = properties;
        this.excelInputService = excelInputService;
        this.excelOutputService = excelOutputService;
        this.weatherClient = weatherClient;
        this.activityService = activityService;
    }

    public ExcelActivityResult checkConfiguredExcelInput() {
        Path inputPath = Path.of(properties.excel().inputPath());
        Path outputPath = Path.of(properties.excel().outputPath());
        ExcelActivityRequest request = excelInputService.read(inputPath);
        WeatherApiResponse weather = weatherClient.getForecast(
                requiredWeatherApiKey(),
                request.location(),
                request.daysAhead() + 1
        );

        List<SportResult> results = activityService.calculateResults(request.sports(), weather);
        int resultCount = excelOutputService.write(results, outputPath, properties.excel().dryRun());
        return new ExcelActivityResult(request.location(), outputPath, resultCount, results);
    }

    private String requiredWeatherApiKey() {
        String apiKey = System.getenv("WEATHER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Missing WEATHER_API_KEY environment variable.");
        }

        return apiKey;
    }
}

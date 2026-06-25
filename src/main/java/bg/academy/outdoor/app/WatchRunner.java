package bg.academy.outdoor.app;

import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.notification.NotificationService;
import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;

import java.time.LocalDateTime;
import java.util.List;

public class WatchRunner {
    private final AppConfig config;
    private final ForecastActivityFacade forecastActivityFacade;
    private final NotificationService notificationService;
    private final ErrorHandler errorHandler;

    public WatchRunner(
            AppConfig config,
            ForecastActivityFacade forecastActivityFacade,
            NotificationService notificationService,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.forecastActivityFacade = forecastActivityFacade;
        this.notificationService = notificationService;
        this.errorHandler = errorHandler;
    }

    public void run() {
        if (!config.watch().enabled()) {
            System.out.println("Watch mode is disabled in config.");
            return;
        }

        while (true) {
            try {
                System.out.println(LocalDateTime.now() + " Checking forecast...");
                System.out.println("Forecast fetch starts.");
                List<SportResult> results = forecastActivityFacade.calculate();
                System.out.println("Forecast fetch succeeds.");
                System.out.println("Calculated sport results: " + results.size());
                if (countIntervals(results) == 0) {
                    System.out.println("No suitable intervals found.");
                }
                notificationService.process(results);
            } catch (RuntimeException e) {
                errorHandler.handle(e);
            }

            sleepUntilNextCheck();
        }
    }

    private int countIntervals(List<SportResult> results) {
        int intervalCount = 0;

        for (SportResult sportResult : results) {
            for (DayResult day : sportResult.days()) {
                intervalCount += day.hours().size();
            }
        }

        return intervalCount;
    }

    private void sleepUntilNextCheck() {
        try {
            Thread.sleep(config.watch().checkEveryMinutes() * 60_000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Watch mode was interrupted", e);
        }
    }
}

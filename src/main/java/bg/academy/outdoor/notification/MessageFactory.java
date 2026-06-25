package bg.academy.outdoor.notification;

import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;

import java.time.LocalDateTime;

public class MessageFactory {
    public String buildEmailSubject() {
        return "Подходящо време за спорт";
    }

    public String buildEmailBody(AppConfig config, SportResult sportResult, DayResult day, String interval) {
        return """
                Sport: %s
                Sport name: %s
                Location: %s
                Date: %s
                Interval: %s
                Timestamp: %s

                This message was generated automatically.
                """.formatted(
                sportResult.sport(),
                sportResult.sportName(),
                config.location(),
                day.date(),
                interval,
                LocalDateTime.now()
        );
    }

    public String buildCalendarSummary(SportResult sportResult) {
        return sportResult.sport() + " - подходящо време";
    }

    public String buildCalendarDescription(AppConfig config, DayResult day, String interval) {
        return """
                Автоматично създадено събитие.
                Локация: %s
                Дата: %s
                Интервал: %s
                Условията покриват зададените критерии.
                """.formatted(config.location(), day.date(), interval);
    }
}

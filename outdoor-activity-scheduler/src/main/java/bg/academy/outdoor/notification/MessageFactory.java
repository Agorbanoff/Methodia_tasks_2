package bg.academy.outdoor.notification;

import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageFactory {
    public String buildEmailSubject() {
        return "Подходящо време за спорт";
    }

    public String buildEmailBody(
            String location,
            SportResult sportResult,
            DayResult day,
            String interval
    ) {
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
                location,
                day.date(),
                interval,
                LocalDateTime.now()
        );
    }

    public String buildCalendarSummary(SportResult sportResult) {
        return sportResult.sport() + " - подходящо време";
    }

    public String buildCalendarDescription(String location, DayResult day, String interval) {
        return """
                Автоматично създадено събитие.
                Локация: %s
                Дата: %s
                Интервал: %s
                Условията покриват зададените критерии.
                """.formatted(location, day.date(), interval);
    }
}

package bg.academy.outdoor.config;

import java.util.List;

public record AppConfig(
        String location,
        int daysAhead,
        List<SportConfig> sports,
        WatchConfig watch,
        EmailConfig email,
        CalendarConfig calendar
) {
}

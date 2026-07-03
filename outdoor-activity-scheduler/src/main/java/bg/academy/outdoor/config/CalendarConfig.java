package bg.academy.outdoor.config;

public record CalendarConfig(
        boolean enabled,
        String credentialsPath,
        String tokensDirectoryPath,
        String calendarId
) {
}

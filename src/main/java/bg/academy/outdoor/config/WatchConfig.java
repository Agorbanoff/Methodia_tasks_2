package bg.academy.outdoor.config;

public record WatchConfig(
        boolean enabled,
        int checkEveryMinutes
) {
}

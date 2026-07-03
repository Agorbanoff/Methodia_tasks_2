package bg.academy.outdoor.config;

public record SportConfig(
        String name,
        String displayName,
        double minTempC,
        double maxTempC,
        double maxGustKph,
        int maxChanceOfRain,
        boolean requiresDaylight,
        int minConsecutiveHours,
        boolean preferWeekend,
        Integer preferCloudAbove
) {
}

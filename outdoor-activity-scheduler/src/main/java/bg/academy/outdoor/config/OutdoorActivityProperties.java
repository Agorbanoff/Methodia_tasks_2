package bg.academy.outdoor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record OutdoorActivityProperties(
        int daysAhead,
        EmailConfig email,
        CalendarConfig calendar,
        ScheduleConfig schedule,
        ExcelConfig excel
) {
    public record ScheduleConfig(
            boolean enabled,
            String cron
    ) {
    }

    public record ExcelConfig(
            String inputPath,
            String outputPath,
            boolean runOnStartup,
            boolean dryRun,
            boolean createTemplateIfMissing
    ) {
    }
}

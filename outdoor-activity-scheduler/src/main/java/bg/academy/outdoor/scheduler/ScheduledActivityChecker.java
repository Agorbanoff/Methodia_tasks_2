package bg.academy.outdoor.scheduler;

import bg.academy.outdoor.config.OutdoorActivityProperties;
import bg.academy.outdoor.excel.ExcelActivityResult;
import bg.academy.outdoor.excel.ExcelActivityService;
import bg.academy.outdoor.notification.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledActivityChecker {
    private final OutdoorActivityProperties properties;
    private final ExcelActivityService excelActivityService;
    private final NotificationService notificationService;

    public ScheduledActivityChecker(
            OutdoorActivityProperties properties,
            ExcelActivityService excelActivityService,
            NotificationService notificationService
    ) {
        this.properties = properties;
        this.excelActivityService = excelActivityService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.schedule.cron}")
    public void checkActivities() {
        if (!properties.schedule().enabled()) {
            return;
        }

        System.out.println(LocalDateTime.now() + " Running scheduled Excel activity check.");
        System.out.println("Reading Excel input: " + properties.excel().inputPath());
        ExcelActivityResult result = excelActivityService.checkConfiguredExcelInput();
        System.out.println("Wrote Excel output: " + result.outputPath());
        System.out.println("Excel result count: " + result.resultCount());
        processNotifications(result);
    }

    private void processNotifications(ExcelActivityResult result) {
        if (properties.excel().dryRun()) {
            System.out.println("Dry-run enabled. Skipping email, calendar, and notification history.");
            return;
        }

        notificationService.process(result.results(), result.location());
    }
}

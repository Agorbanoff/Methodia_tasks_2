package bg.academy.outdoor.excel;

import bg.academy.outdoor.config.OutdoorActivityProperties;
import bg.academy.outdoor.notification.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ExcelStartupRunner implements CommandLineRunner {
    private final OutdoorActivityProperties properties;
    private final ExcelActivityService excelActivityService;
    private final ExcelTemplateService excelTemplateService;
    private final NotificationService notificationService;

    public ExcelStartupRunner(
            OutdoorActivityProperties properties,
            ExcelActivityService excelActivityService,
            ExcelTemplateService excelTemplateService,
            NotificationService notificationService
    ) {
        this.properties = properties;
        this.excelActivityService = excelActivityService;
        this.excelTemplateService = excelTemplateService;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) {
        Path inputPath = Path.of(properties.excel().inputPath());
        if (properties.excel().createTemplateIfMissing()
                && excelTemplateService.createTemplateIfMissing(inputPath)) {
            System.out.println("Created Excel input template: " + inputPath);
        }

        if (!properties.excel().runOnStartup()) {
            return;
        }

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

package bg.academy.outdoor.notification;

import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;

import java.util.List;

public class NotificationService {
    private final AppConfig config;
    private final NotificationHistoryService notificationHistoryService;
    private final NotificationKeyFactory notificationKeyFactory;
    private final NotificationActionService notificationActionService;

    public NotificationService(
            AppConfig config,
            NotificationHistoryService notificationHistoryService,
            NotificationKeyFactory notificationKeyFactory,
            NotificationActionService notificationActionService
    ) {
        this.config = config;
        this.notificationHistoryService = notificationHistoryService;
        this.notificationKeyFactory = notificationKeyFactory;
        this.notificationActionService = notificationActionService;
    }

    public void process(List<SportResult> results) {
        for (SportResult sportResult : results) {
            for (DayResult day : sportResult.days()) {
                for (String interval : day.hours()) {
                    String notificationKey = notificationKeyFactory.build(sportResult, day, interval);
                    System.out.println("Notification key: " + notificationKey);

                    if (notificationHistoryService.wasNotified(notificationKey)) {
                        System.out.println("Already notified: " + notificationKey);
                    } else {
                        System.out.println("New notification candidate: " + notificationKey);
                        System.out.println("NEW MATCH: " + sportResult.sport() + " " + day.date() + " " + interval);

                        if (notificationActionService.execute(config, sportResult, day, interval, notificationKey)) {
                            notificationHistoryService.markNotified(notificationKey);
                            System.out.println("Marked as notified: " + notificationKey);
                        }
                    }
                }
            }
        }
    }
}

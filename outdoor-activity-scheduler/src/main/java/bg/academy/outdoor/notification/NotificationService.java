package bg.academy.outdoor.notification;

import bg.academy.outdoor.config.OutdoorActivityProperties;
import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final OutdoorActivityProperties properties;
    private final NotificationHistoryService notificationHistoryService;
    private final NotificationKeyFactory notificationKeyFactory;
    private final NotificationActionService notificationActionService;

    public NotificationService(
            OutdoorActivityProperties properties,
            NotificationHistoryService notificationHistoryService,
            NotificationKeyFactory notificationKeyFactory,
            NotificationActionService notificationActionService
    ) {
        this.properties = properties;
        this.notificationHistoryService = notificationHistoryService;
        this.notificationKeyFactory = notificationKeyFactory;
        this.notificationActionService = notificationActionService;
    }

    public void process(List<SportResult> results, String location) {
        if (!notificationsEnabled()) {
            System.out.println("Email and calendar notifications are disabled. Skipping notification history.");
            return;
        }

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

                        if (notificationActionService.execute(
                                properties,
                                location,
                                sportResult,
                                day,
                                interval,
                                notificationKey
                        )) {
                            notificationHistoryService.markNotified(notificationKey);
                            System.out.println("Marked as notified: " + notificationKey);
                        }
                    }
                }
            }
        }
    }

    private boolean notificationsEnabled() {
        return properties.email().enabled() || properties.calendar().enabled();
    }
}

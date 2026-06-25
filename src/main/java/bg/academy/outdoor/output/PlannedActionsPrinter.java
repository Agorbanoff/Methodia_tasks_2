package bg.academy.outdoor.output;

import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.notification.MessageFactory;
import bg.academy.outdoor.notification.NotificationKeyFactory;

import java.util.List;

public class PlannedActionsPrinter {
    private final AppConfig config;
    private final NotificationKeyFactory notificationKeyFactory;
    private final MessageFactory messageFactory;

    public PlannedActionsPrinter(
            AppConfig config,
            NotificationKeyFactory notificationKeyFactory,
            MessageFactory messageFactory
    ) {
        this.config = config;
        this.notificationKeyFactory = notificationKeyFactory;
        this.messageFactory = messageFactory;
    }

    public void print(List<SportResult> results) {
        System.out.println();
        System.out.println("Planned actions:");

        for (SportResult sportResult : results) {
            for (DayResult day : sportResult.days()) {
                for (String interval : day.hours()) {
                    System.out.println("- sport: " + sportResult.sport());
                    System.out.println("  sportName: " + sportResult.sportName());
                    System.out.println("  date: " + day.date());
                    System.out.println("  interval: " + interval);
                    System.out.println("  notification key: "
                            + notificationKeyFactory.build(sportResult, day, interval));
                    System.out.println("  email would be sent: " + config.email().enabled());
                    if (config.email().enabled()) {
                        System.out.println("  email recipient: " + config.email().to());
                    }
                    System.out.println("  calendar event would be created: " + config.calendar().enabled());
                    if (config.calendar().enabled()) {
                        System.out.println("  calendar summary: " + messageFactory.buildCalendarSummary(sportResult));
                    }
                }
            }
        }
    }
}

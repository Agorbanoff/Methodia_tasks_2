package bg.academy.outdoor.notification;

import bg.academy.outdoor.calendar.GoogleCalendarService;
import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.email.EmailService;
import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;

import java.time.LocalDate;

public class NotificationActionService {
    private final EmailService emailService;
    private final GoogleCalendarService googleCalendarService;
    private final MessageFactory messageFactory;

    public NotificationActionService(
            EmailService emailService,
            GoogleCalendarService googleCalendarService,
            MessageFactory messageFactory
    ) {
        this.emailService = emailService;
        this.googleCalendarService = googleCalendarService;
        this.messageFactory = messageFactory;
    }

    public boolean execute(
            AppConfig config,
            SportResult sportResult,
            DayResult day,
            String interval,
            String notificationKey
    ) {
        boolean succeeded = true;

        if (config.calendar().enabled()) {
            try {
                System.out.println("Creating calendar event for " + notificationKey);
                googleCalendarService.createEventIfNotExists(
                        config.calendar(),
                        messageFactory.buildCalendarSummary(sportResult),
                        messageFactory.buildCalendarDescription(config, day, interval),
                        LocalDate.parse(day.date()),
                        interval,
                        config.location()
                );
                System.out.println("Calendar event created for " + notificationKey);
            } catch (RuntimeException e) {
                succeeded = false;
                System.err.println("Failed to create calendar event for " + notificationKey + ": " + e.getMessage());
            }
        }

        if (config.email().enabled()) {
            try {
                System.out.println("Sending email for " + notificationKey);
                emailService.sendEmail(
                        config.email().to(),
                        messageFactory.buildEmailSubject(),
                        messageFactory.buildEmailBody(config, sportResult, day, interval)
                );
                System.out.println("Email sent for " + notificationKey);
            } catch (RuntimeException e) {
                succeeded = false;
                System.err.println("Failed to send email for " + notificationKey + ": " + e.getMessage());
            }
        }

        return succeeded;
    }
}

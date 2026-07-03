package bg.academy.outdoor.calendar;

import bg.academy.outdoor.config.CalendarConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "Outdoor Activity";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR_EVENTS);
    private static final ZoneId TIME_ZONE = ZoneId.of("Europe/Sofia");
    private static final String OAUTH_HOST = "localhost";
    private static final int OAUTH_PORT = 8888;
    private static final String OAUTH_CALLBACK_PATH = "/Callback";
    private static final String OAUTH_CALLBACK_URL = "http://localhost:8888/Callback";

    public void createEventIfNotExists(
            CalendarConfig config,
            String summary,
            String description,
            LocalDate date,
            String interval,
            String location
    ) {
        try {
            Calendar calendar = createCalendarClient(config);
            String calendarId = calendarId(config);
            TimeRange timeRange = parseTimeRange(date, interval);

            if (eventExists(calendar, calendarId, summary, timeRange)) {
                return;
            }

            Event event = new Event()
                    .setSummary(summary)
                    .setDescription(description)
                    .setLocation(location)
                    .setStart(eventDateTime(timeRange.start()))
                    .setEnd(eventDateTime(timeRange.end()));

            calendar.events().insert(calendarId, event).execute();
        } catch (IOException | GeneralSecurityException e) {
            printExceptionDetails(e);
            throw new RuntimeException(buildExceptionMessage("Failed to create Google Calendar event", e), e);
        }
    }

    private Calendar createCalendarClient(CalendarConfig config) throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport, config);

        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize(NetHttpTransport httpTransport, CalendarConfig config) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(config.credentialsPath()),
                StandardCharsets.UTF_8
        )) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientSecrets,
                    SCOPES
            )
                    .setDataStoreFactory(new FileDataStoreFactory(Path.of(config.tokensDirectoryPath()).toFile()))
                    .setAccessType("offline")
                    .build();

            ensureOAuthPortAvailable();
            System.out.println("Starting Google OAuth flow on " + OAUTH_CALLBACK_URL);
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setHost(OAUTH_HOST)
                    .setPort(OAUTH_PORT)
                    .setCallbackPath(OAUTH_CALLBACK_PATH)
                    .build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
    }

    private void ensureOAuthPortAvailable() {
        try (ServerSocket ignored = new ServerSocket(OAUTH_PORT)) {
            // The socket is closed immediately; LocalServerReceiver binds the port for the OAuth callback.
        } catch (IOException e) {
            throw new RuntimeException("Google OAuth local callback port 8888 is already in use.", e);
        }
    }

    private void printExceptionDetails(Exception exception) {
        Throwable rootCause = rootCause(exception);

        System.err.println("Google Calendar exception class: " + exception.getClass().getName());
        System.err.println("Google Calendar exception message: " + exception.getMessage());
        System.err.println("Google Calendar root cause class: " + rootCause.getClass().getName());
        System.err.println("Google Calendar root cause message: " + rootCause.getMessage());
        exception.printStackTrace(System.err);
    }

    private String buildExceptionMessage(String prefix, Exception exception) {
        Throwable rootCause = rootCause(exception);

        return prefix
                + " [exceptionClass=" + exception.getClass().getName()
                + ", exceptionMessage=" + exception.getMessage()
                + ", rootCauseClass=" + rootCause.getClass().getName()
                + ", rootCauseMessage=" + rootCause.getMessage()
                + "]";
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }

    private boolean eventExists(
            Calendar calendar,
            String calendarId,
            String summary,
            TimeRange timeRange
    ) throws IOException {
        DateTime dayStart = dateTime(timeRange.start().toLocalDate().atStartOfDay());
        DateTime nextDayStart = dateTime(timeRange.start().toLocalDate().plusDays(1).atStartOfDay());

        Events events = calendar.events()
                .list(calendarId)
                .setTimeMin(dayStart)
                .setTimeMax(nextDayStart)
                .setSingleEvents(true)
                .execute();

        if (events.getItems() == null) {
            return false;
        }

        DateTime targetStart = dateTime(timeRange.start());
        DateTime targetEnd = dateTime(timeRange.end());

        return events.getItems().stream()
                .anyMatch(event -> summary.equals(event.getSummary())
                        && sameDateTime(targetStart, event.getStart())
                        && sameDateTime(targetEnd, event.getEnd()));
    }

    private boolean sameDateTime(DateTime expected, EventDateTime actual) {
        return actual != null
                && actual.getDateTime() != null
                && expected.getValue() == actual.getDateTime().getValue();
    }

    private EventDateTime eventDateTime(LocalDateTime localDateTime) {
        return new EventDateTime()
                .setDateTime(dateTime(localDateTime))
                .setTimeZone(TIME_ZONE.getId());
    }

    private DateTime dateTime(LocalDateTime localDateTime) {
        ZonedDateTime zonedDateTime = localDateTime.atZone(TIME_ZONE);
        return new DateTime(zonedDateTime.toInstant().toEpochMilli());
    }

    private TimeRange parseTimeRange(LocalDate date, String interval) {
        String[] parts = interval.split("-");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid calendar interval: " + interval);
        }

        int startHour = parseHour(parts[0]);
        int endHour = parseHour(parts[1]);

        return new TimeRange(
                LocalDateTime.of(date, LocalTime.of(startHour, 0)),
                LocalDateTime.of(date, LocalTime.of(endHour, 0))
        );
    }

    private int parseHour(String value) {
        String trimmed = value.trim();
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex >= 0) {
            trimmed = trimmed.substring(0, colonIndex);
        }

        return Integer.parseInt(trimmed);
    }

    private String calendarId(CalendarConfig config) {
        if (config.calendarId() == null || config.calendarId().isBlank()) {
            return "primary";
        }

        return config.calendarId();
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }
}

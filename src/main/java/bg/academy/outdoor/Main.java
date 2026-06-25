package bg.academy.outdoor;
import bg.academy.outdoor.app.*;
import bg.academy.outdoor.calendar.GoogleCalendarService;
import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.config.ConfigLoader;
import bg.academy.outdoor.email.EmailService;
import bg.academy.outdoor.notification.*;
import bg.academy.outdoor.output.PlannedActionsPrinter;
import bg.academy.outdoor.service.ActivityService;
import bg.academy.outdoor.service.IntervalService;
import bg.academy.outdoor.service.SportMatcher;
import bg.academy.outdoor.weather.WeatherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Main {
    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        try {
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            AppConfig config = new ConfigLoader(objectMapper).loadConfig();
            RunMode mode = RunMode.fromArgs(args);
            NotificationKeyFactory keyFactory = new NotificationKeyFactory();
            MessageFactory messageFactory = new MessageFactory();
            ForecastActivityFacade facade = new ForecastActivityFacade(config, new WeatherClient(objectMapper),
                    new ActivityService(new SportMatcher(), new IntervalService()));
            NotificationActionService actionService = new NotificationActionService(new EmailService(),
                    new GoogleCalendarService(), messageFactory);
            NotificationService notificationService = new NotificationService(config,
                    new NotificationHistoryService(objectMapper), keyFactory, actionService);
            new ApplicationRunner(
                    new OnceRunner(objectMapper, facade),
                    new DryRunRunner(objectMapper, facade, new PlannedActionsPrinter(config, keyFactory, messageFactory)),
                    new WatchRunner(config, facade, notificationService, errorHandler)
            ).run(mode);
        } catch (Exception e) {
            errorHandler.handle(e);
        }
    }
}

package bg.academy.outdoor.app;

public class ErrorHandler {
    public void handle(Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            System.err.println(exception.getMessage());
            return;
        }

        String message = exception.getMessage();

        if (message != null && message.startsWith("Missing resource:")) {
            System.err.println("Missing config resource: " + message.substring("Missing resource:".length()).trim());
            return;
        }

        if ("Missing WEATHER_API_KEY environment variable.".equals(message)) {
            System.err.println(message);
            return;
        }

        if (message != null && message.startsWith("WeatherAPI request failed")) {
            System.err.println("WeatherAPI HTTP error: " + message);
            return;
        }

        System.err.println("Unexpected exception: " + message);
        exception.printStackTrace(System.err);
    }
}

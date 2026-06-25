package bg.academy.outdoor.app;

import bg.academy.outdoor.output.SportResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class OnceRunner {
    private final ObjectMapper objectMapper;
    private final ForecastActivityFacade forecastActivityFacade;

    public OnceRunner(ObjectMapper objectMapper, ForecastActivityFacade forecastActivityFacade) {
        this.objectMapper = objectMapper;
        this.forecastActivityFacade = forecastActivityFacade;
    }

    public void run() throws Exception {
        List<SportResult> results = forecastActivityFacade.calculate();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
    }
}

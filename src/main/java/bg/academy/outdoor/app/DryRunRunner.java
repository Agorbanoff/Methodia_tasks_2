package bg.academy.outdoor.app;

import bg.academy.outdoor.output.PlannedActionsPrinter;
import bg.academy.outdoor.output.SportResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DryRunRunner {
    private final ObjectMapper objectMapper;
    private final ForecastActivityFacade forecastActivityFacade;
    private final PlannedActionsPrinter plannedActionsPrinter;

    public DryRunRunner(
            ObjectMapper objectMapper,
            ForecastActivityFacade forecastActivityFacade,
            PlannedActionsPrinter plannedActionsPrinter
    ) {
        this.objectMapper = objectMapper;
        this.forecastActivityFacade = forecastActivityFacade;
        this.plannedActionsPrinter = plannedActionsPrinter;
    }

    public void run() throws Exception {
        List<SportResult> results = forecastActivityFacade.calculate();
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));
        plannedActionsPrinter.print(results);
    }
}

package bg.academy.outdoor.excel;

import bg.academy.outdoor.config.SportConfig;

import java.util.List;

public record ExcelActivityRequest(
        String location,
        int daysAhead,
        List<SportConfig> sports
) {
}

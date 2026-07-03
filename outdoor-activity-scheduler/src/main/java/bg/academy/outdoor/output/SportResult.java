package bg.academy.outdoor.output;

import java.util.List;

public record SportResult(
        String sport,
        String sportName,
        List<DayResult> days
) {
}

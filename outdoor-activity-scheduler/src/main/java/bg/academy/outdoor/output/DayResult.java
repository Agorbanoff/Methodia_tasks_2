package bg.academy.outdoor.output;

import java.util.List;

public record DayResult(
        String date,
        List<String> hours,
        boolean preferred
) {
}

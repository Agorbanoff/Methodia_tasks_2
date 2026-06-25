package bg.academy.outdoor.notification;

import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;

public class NotificationKeyFactory {
    public String build(SportResult sportResult, DayResult day, String interval) {
        return sportResult.sportName() + "|" + day.date() + "|" + interval;
    }
}

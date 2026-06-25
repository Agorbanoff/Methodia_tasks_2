package bg.academy.outdoor.service;

import bg.academy.outdoor.config.AppConfig;
import bg.academy.outdoor.config.SportConfig;
import bg.academy.outdoor.output.DayResult;
import bg.academy.outdoor.output.SportResult;
import bg.academy.outdoor.weather.dto.ForecastDay;
import bg.academy.outdoor.weather.dto.HourWeather;
import bg.academy.outdoor.weather.dto.WeatherApiResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityService {
    private final SportMatcher sportMatcher;
    private final IntervalService intervalService;

    public ActivityService(SportMatcher sportMatcher, IntervalService intervalService) {
        this.sportMatcher = sportMatcher;
        this.intervalService = intervalService;
    }

    public List<SportResult> calculateResults(AppConfig config, WeatherApiResponse weather) {
        List<SportResult> results = new ArrayList<>();

        for (SportConfig sport : config.sports()) {
            List<DayResult> days = new ArrayList<>();

            for (ForecastDay forecastDay : weather.forecast().forecastday()) {
                List<Integer> suitableHours = sportMatcher.findSuitableHours(forecastDay, sport);
                List<String> intervals = intervalService.buildIntervals(suitableHours, sport.minConsecutiveHours());

                if (!intervals.isEmpty()) {
                    days.add(new DayResult(
                            forecastDay.date(),
                            intervals,
                            isPreferredDay(sport, forecastDay, suitableHours)
                    ));
                }
            }

            results.add(new SportResult(sport.displayName(), sport.name(), days));
        }

        return results;
    }

    private boolean isPreferredDay(SportConfig sport, ForecastDay day, List<Integer> suitableHours) {
        return isPreferredWeekend(sport, day.date()) || isPreferredCloud(sport, day, suitableHours);
    }

    private boolean isPreferredWeekend(SportConfig sport, String date) {
        if (!sport.preferWeekend()) {
            return false;
        }

        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isPreferredCloud(SportConfig sport, ForecastDay day, List<Integer> suitableHours) {
        if (sport.preferCloudAbove() == null) {
            return false;
        }

        return day.hour().stream()
                .filter(hour -> suitableHours.contains(hourOf(hour)))
                .anyMatch(hour -> hour.cloud() > sport.preferCloudAbove());
    }

    private int hourOf(HourWeather weather) {
        return Integer.parseInt(weather.time().substring(11, 13));
    }
}

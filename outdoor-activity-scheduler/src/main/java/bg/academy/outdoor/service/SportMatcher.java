package bg.academy.outdoor.service;

import bg.academy.outdoor.config.SportConfig;
import bg.academy.outdoor.weather.dto.ForecastDay;
import bg.academy.outdoor.weather.dto.HourWeather;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class SportMatcher {
    private static final DateTimeFormatter HOUR_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter SUN_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    public List<Integer> findSuitableHours(ForecastDay day, SportConfig sport) {
        List<Integer> suitableHours = new ArrayList<>();

        Integer sunriseMinute = null;
        Integer sunsetMinute = null;
        if (sport.requiresDaylight()) {
            sunriseMinute = minuteOfDay(LocalTime.parse(day.astro().sunrise(), SUN_TIME_FORMATTER));
            sunsetMinute = minuteOfDay(LocalTime.parse(day.astro().sunset(), SUN_TIME_FORMATTER));
        }

        for (HourWeather weather : day.hour()) {
            LocalDateTime time = LocalDateTime.parse(weather.time(), HOUR_TIME_FORMATTER);
            int hour = time.getHour();

            if (!matchesWeatherConditions(sport, weather)) {
                continue;
            }

            if (sport.requiresDaylight() && !isFullyInsideDaylight(hour, sunriseMinute, sunsetMinute)) {
                continue;
            }

            suitableHours.add(hour);
        }

        return suitableHours;
    }

    public boolean matches(SportConfig sport, HourWeather weather) {
        return matchesWeatherConditions(sport, weather);
    }

    private boolean matchesWeatherConditions(SportConfig sport, HourWeather weather) {
        boolean temperatureMatches = weather.temp_c() >= sport.minTempC()
                && weather.temp_c() <= sport.maxTempC();
        boolean windMatches = weather.gust_kph() < sport.maxGustKph();
        boolean rainMatches = weather.chance_of_rain() <= sport.maxChanceOfRain();

        return temperatureMatches && windMatches && rainMatches;
    }

    private boolean isFullyInsideDaylight(int hour, int sunriseMinute, int sunsetMinute) {
        int intervalStartMinute = hour * 60;
        int intervalEndMinute = (hour + 1) * 60;

        return intervalStartMinute >= sunriseMinute && intervalEndMinute <= sunsetMinute;
    }

    private int minuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}

package bg.academy.outdoor.service;

import bg.academy.outdoor.weather.dto.HourWeather;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class IntervalService {
    public List<String> buildIntervals(List<Integer> suitableHours, int minConsecutiveHours) {
        List<Integer> sortedHours = suitableHours.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        List<String> intervals = new ArrayList<>();

        int index = 0;
        while (index < sortedHours.size()) {
            int startHour = sortedHours.get(index);
            int endHour = startHour + 1;
            index++;

            while (index < sortedHours.size() && sortedHours.get(index) == endHour) {
                endHour++;
                index++;
            }

            if (endHour - startHour >= minConsecutiveHours) {
                intervals.add(formatInterval(startHour, endHour));
            }
        }

        return intervals;
    }

    public List<HourWeather> filterByHourRange(List<HourWeather> hours, int startHour, int endHour) {
        return hours.stream()
                .filter(hour -> {
                    int hourValue = Integer.parseInt(hour.time().substring(11, 13));
                    return hourValue >= startHour && hourValue <= endHour;
                })
                .toList();
    }

    private String formatInterval(int startHour, int endHour) {
        return startHour + "-" + endHour;
    }
}

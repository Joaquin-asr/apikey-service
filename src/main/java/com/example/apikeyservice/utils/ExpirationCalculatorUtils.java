package com.example.apikeyservice.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class ExpirationCalculatorUtils {

    @Value("${apikey.expiration-day}")
    private String expirationDay;

    public LocalDateTime calculateNextExpiration(LocalDateTime from) {
        DayOfWeek targetDay = DayOfWeek.valueOf(expirationDay.toUpperCase());
        DayOfWeek currentDay = from.getDayOfWeek();

        int daysUntil = (targetDay.getValue() - currentDay.getValue() + 7) % 7;
        if (daysUntil == 0) daysUntil = 7;

        return from.toLocalDate().plusDays(daysUntil).atTime(LocalTime.MIDNIGHT);
    }
}

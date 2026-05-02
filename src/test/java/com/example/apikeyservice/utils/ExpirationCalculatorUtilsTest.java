package com.example.apikeyservice.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpirationCalculatorUtilsTest {

    private ExpirationCalculatorUtils calculator;

    @BeforeEach
    void setUp() {
        calculator = new ExpirationCalculatorUtils();
        ReflectionTestUtils.setField(calculator, "expirationDay", "MONDAY");
    }

    @Test
    void calculateNextExpiration_sameDay_returns7DaysLater() {
        LocalDateTime monday = LocalDate.of(2024, 1, 1).atTime(10, 0); // lunes

        LocalDateTime result = calculator.calculateNextExpiration(monday);

        assertEquals(LocalDate.of(2024, 1, 8).atTime(LocalTime.MIDNIGHT), result);
    }

    @Test
    void calculateNextExpiration_dayBefore_returnsCorrectDays() {
        LocalDateTime friday = LocalDate.of(2024, 1, 5).atTime(10, 0); // viernes → 3 días hasta lunes

        LocalDateTime result = calculator.calculateNextExpiration(friday);

        assertEquals(LocalDate.of(2024, 1, 8).atTime(LocalTime.MIDNIGHT), result);
    }

    @Test
    void calculateNextExpiration_midWeek_returnsCorrectDays() {
        LocalDateTime wednesday = LocalDate.of(2024, 1, 3).atTime(10, 0); // miércoles → 5 días hasta lunes

        LocalDateTime result = calculator.calculateNextExpiration(wednesday);

        assertEquals(LocalDate.of(2024, 1, 8).atTime(LocalTime.MIDNIGHT), result);
    }
}

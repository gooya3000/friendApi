package com.example.aprbackendassignment.common.util;

import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 슬라이딩 윈도우 처리를 위한 유틸 클래스입니다.
 */
@NoArgsConstructor
public class WindowParser {
    public static Instant getInstant(String window, Clock clock) {
        if (window == null || window.isBlank() || "over".equalsIgnoreCase(window)) {
            return Instant.EPOCH;
        }
        return switch (window) {
            case "1d" -> Instant.now(clock).minus(1, ChronoUnit.DAYS);
            case "7d" -> Instant.now(clock).minus(7, ChronoUnit.DAYS);
            case "30d" -> Instant.now(clock).minus(30, ChronoUnit.DAYS);
            case "90d" -> Instant.now(clock).minus(90, ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Invalid window: " + window);
        };
    }

}

package com.example.aprbackendassignment.common.util;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 슬라이딩 윈도우 처리를 위한 유틸 클래스.
 */
@NoArgsConstructor
public class Window {
    public static Instant getInstant(String window) {
        if (window == null || window.isBlank() || "over".equalsIgnoreCase(window)) {
            return Instant.EPOCH;
        }
        switch (window) {
            case "1d":  return Instant.now().minus(1, ChronoUnit.DAYS);
            case "7d":  return Instant.now().minus(7, ChronoUnit.DAYS);
            case "30d": return Instant.now().minus(30, ChronoUnit.DAYS);
            case "90d": return Instant.now().minus(90, ChronoUnit.DAYS);
            default: throw new IllegalArgumentException("Invalid window: " + window);
        }
    }

}

package com.deskflow.shared.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@UtilityClass
public class DateTimeUtil {

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static LocalDateTime plusHours(LocalDateTime base, long hours) {
        return base.plusHours(hours);
    }

    public static boolean isAfter(LocalDateTime time, LocalDateTime reference) {
        return time.isAfter(reference);
    }

    public static boolean isBefore(LocalDateTime time, LocalDateTime reference) {
        return time.isBefore(reference);
    }
}
package com.project.articket.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    // 일자까지만 출력 (예: 2026-09-22)
    public static String toDateString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(YYYY_MM_DD);
    }
    // 일자까지, LocalDate타입 오버로드 메서드
    public static String toDateString(LocalDate date) {
        if (date == null) return null;
        return date.format(YYYY_MM_DD);
    }

    // 시/분까지 출력 (예: 2026-09-22 17:59)
    public static String toDateTimeMinuteString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(YYYY_MM_DD_HH_MM);
    }

    // 시/분/초까지 출력 (예: 2026-09-22 17:59:07)
    public static String toDateTimeSecondString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(YYYY_MM_DD_HH_MM_SS);
    }

}

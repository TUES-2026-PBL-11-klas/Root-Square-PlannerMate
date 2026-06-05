package com.rootsquare.planmate.service;

import com.rootsquare.planmate.dto.ScheduleItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class CalendarExportService {

    private static final DateTimeFormatter GOOGLE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_LOCAL_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter ICS_UTC_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String generateGoogleCalendarUrl(ScheduleItemResponse item) {
        return UriComponentsBuilder
                .fromUriString("https://calendar.google.com/calendar/render")
                .queryParam("action", "TEMPLATE")
                .queryParam("text", item.getTitle())
                .queryParam("dates", googleDateRange(item))
                .queryParam("details", item.getDescription())
                .queryParam("location", item.getLocation())
                .build()
                .encode()
                .toUriString();
    }

    public String generateAppleCalendarFile(ScheduleItemResponse item) {
        return String.join("\r\n",
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//PlanMate//Schedule Maker//EN",
                "CALSCALE:GREGORIAN",
                "METHOD:PUBLISH",
                "BEGIN:VEVENT",
                "UID:" + item.getId() + "@planmate.local",
                "DTSTAMP:" + OffsetDateTime.now(ZoneOffset.UTC).format(ICS_UTC_FORMAT),
                "DTSTART:" + eventStart(item).format(ICS_LOCAL_FORMAT),
                "DTEND:" + eventEnd(item).format(ICS_LOCAL_FORMAT),
                "SUMMARY:" + escapeIcs(item.getTitle()),
                "DESCRIPTION:" + escapeIcs(item.getDescription()),
                "LOCATION:" + escapeIcs(item.getLocation()),
                "END:VEVENT",
                "END:VCALENDAR",
                ""
        );
    }

    public String buildIcsFilename(ScheduleItemResponse item) {
        String slug = item.getTitle().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (slug.isBlank()) {
            slug = "planmate-event";
        }

        return slug + ".ics";
    }

    private String googleDateRange(ScheduleItemResponse item) {
        return eventStart(item).format(GOOGLE_FORMAT) + "/" + eventEnd(item).format(GOOGLE_FORMAT);
    }

    private LocalDateTime eventStart(ScheduleItemResponse item) {
        return LocalDateTime.of(item.getDate(), item.getStartTime());
    }

    private LocalDateTime eventEnd(ScheduleItemResponse item) {
        return LocalDateTime.of(item.getDate(), item.getEndTime());
    }

    private String escapeIcs(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}

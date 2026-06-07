package com.rootsquare.planmate.service;

import com.rootsquare.planmate.dto.ScheduleItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarExportServiceTest {

    private CalendarExportService service;
    private ScheduleItemResponse item;

    @BeforeEach
    void setUp() {
        service = new CalendarExportService();
        item = new ScheduleItemResponse(
                1L,
                "Math revision",
                "Practice equations",
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                "Library",
                false,
                true
        );
    }

    // ── Google Calendar URL ───────────────────────────────────────────────────

    @Test
    void googleCalendarUrl_containsActionTemplate() {
        String url = service.generateGoogleCalendarUrl(item);
        assertThat(url).contains("action=TEMPLATE");
    }

    @Test
    void googleCalendarUrl_containsEncodedTitle() {
        String url = service.generateGoogleCalendarUrl(item);
        // Title is URL-encoded in the query string
        assertThat(url).containsIgnoringCase("Math");
    }

    @Test
    void googleCalendarUrl_containsDateRange() {
        String url = service.generateGoogleCalendarUrl(item);
        // date format: yyyyMMddTHHmmss
        assertThat(url).contains("20260610T090000");
        assertThat(url).contains("20260610T103000");
    }

    @Test
    void googleCalendarUrl_containsLocation() {
        String url = service.generateGoogleCalendarUrl(item);
        assertThat(url).contains("Library");
    }

    @Test
    void googleCalendarUrl_pointsToGoogleCalendar() {
        String url = service.generateGoogleCalendarUrl(item);
        assertThat(url).startsWith("https://calendar.google.com/calendar/render");
    }

    // ── Apple Calendar ICS ────────────────────────────────────────────────────

    @Test
    void appleCalendarFile_hasRequiredIcsStructure() {
        String ics = service.generateAppleCalendarFile(item);
        assertThat(ics).contains("BEGIN:VCALENDAR");
        assertThat(ics).contains("BEGIN:VEVENT");
        assertThat(ics).contains("END:VEVENT");
        assertThat(ics).contains("END:VCALENDAR");
    }

    @Test
    void appleCalendarFile_containsTitle() {
        String ics = service.generateAppleCalendarFile(item);
        assertThat(ics).contains("SUMMARY:Math revision");
    }

    @Test
    void appleCalendarFile_containsStartAndEndTime() {
        String ics = service.generateAppleCalendarFile(item);
        assertThat(ics).contains("DTSTART:20260610T090000");
        assertThat(ics).contains("DTEND:20260610T103000");
    }

    @Test
    void appleCalendarFile_containsUid() {
        String ics = service.generateAppleCalendarFile(item);
        assertThat(ics).contains("UID:1@planmate.local");
    }

    @Test
    void appleCalendarFile_containsDtstamp() {
        String ics = service.generateAppleCalendarFile(item);
        assertThat(ics).contains("DTSTAMP:");
    }

    @Test
    void appleCalendarFile_containsLocation() {
        String ics = service.generateAppleCalendarFile(item);
        assertThat(ics).contains("LOCATION:Library");
    }

    @Test
    void appleCalendarFile_escapesCommaInTitle() {
        ScheduleItemResponse withComma = new ScheduleItemResponse(
                2L, "Math, Physics", "desc",
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, false, true
        );
        String ics = service.generateAppleCalendarFile(withComma);
        assertThat(ics).contains("SUMMARY:Math\\, Physics");
    }

    @Test
    void appleCalendarFile_escapesSemicolonInDescription() {
        ScheduleItemResponse withSemicolon = new ScheduleItemResponse(
                3L, "Title", "Step 1; Step 2",
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, false, true
        );
        String ics = service.generateAppleCalendarFile(withSemicolon);
        assertThat(ics).contains("DESCRIPTION:Step 1\\; Step 2");
    }

    @Test
    void appleCalendarFile_handlesNullDescription() {
        ScheduleItemResponse noDesc = new ScheduleItemResponse(
                4L, "Title", null,
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, false, true
        );
        String ics = service.generateAppleCalendarFile(noDesc);
        assertThat(ics).contains("DESCRIPTION:");
    }

    // ── ICS filename ──────────────────────────────────────────────────────────

    @Test
    void buildIcsFilename_slugifiesTitle() {
        String filename = service.buildIcsFilename(item);
        assertThat(filename).isEqualTo("math-revision.ics");
    }

    @Test
    void buildIcsFilename_handlesSpecialCharacters() {
        ScheduleItemResponse special = new ScheduleItemResponse(
                5L, "Math & Physics! Exam", null,
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, false, true
        );
        String filename = service.buildIcsFilename(special);
        assertThat(filename).endsWith(".ics");
        assertThat(filename).doesNotContain(" ");
        assertThat(filename).doesNotContain("&");
        assertThat(filename).doesNotContain("!");
    }

    @Test
    void buildIcsFilename_fallsBackForBlankTitle() {
        ScheduleItemResponse blankTitle = new ScheduleItemResponse(
                6L, "   ", null,
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, false, true
        );
        String filename = service.buildIcsFilename(blankTitle);
        assertThat(filename).isEqualTo("planmate-event.ics");
    }

    @Test
    void buildIcsFilename_lowercasesTitle() {
        ScheduleItemResponse upper = new ScheduleItemResponse(
                7L, "BIOLOGY LAB", null,
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                null, false, true
        );
        String filename = service.buildIcsFilename(upper);
        assertThat(filename).isEqualTo("biology-lab.ics");
    }
}
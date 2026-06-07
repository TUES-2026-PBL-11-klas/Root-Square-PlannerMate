package com.rootsquare.planmate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleItemRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void missingTitle_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setTitle(null);
        assertThat(violationMessages(r)).contains("title is required");
    }

    @Test
    void blankTitle_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setTitle("   ");
        assertThat(violationMessages(r)).contains("title is required");
    }

    @Test
    void missingDate_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setDate(null);
        assertThat(violationMessages(r)).contains("date is required");
    }

    @Test
    void missingStartTime_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setStartTime(null);
        assertThat(violationMessages(r)).contains("startTime is required");
    }

    @Test
    void missingEndTime_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setEndTime(null);
        assertThat(violationMessages(r)).contains("endTime is required");
    }

    @Test
    void endTimeBeforeStartTime_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setStartTime(LocalTime.of(12, 0));
        r.setEndTime(LocalTime.of(10, 0));
        assertThat(violationMessages(r)).contains("endTime must be after startTime");
    }

    @Test
    void equalStartAndEndTime_hasViolation() {
        ScheduleItemRequest r = validRequest();
        r.setStartTime(LocalTime.of(10, 0));
        r.setEndTime(LocalTime.of(10, 0));
        // endTime must be strictly after startTime
        assertThat(violationMessages(r)).contains("endTime must be after startTime");
    }

    @Test
    void nullStartAndEndTime_doesNotTriggerTimeOrderViolation() {
        // Null times already produce their own required violations; the cross-field
        // validator should return true (not double-report the ordering issue).
        ScheduleItemRequest r = validRequest();
        r.setStartTime(null);
        r.setEndTime(null);
        Set<ConstraintViolation<ScheduleItemRequest>> violations = validator.validate(r);
        // Only the @NotNull violations fire, not the @AssertTrue
        long orderViolations = violations.stream()
                .filter(v -> v.getMessage().contains("after"))
                .count();
        assertThat(orderViolations).isZero();
    }

    @Test
    void activeDefaultsToTrue() {
        ScheduleItemRequest r = new ScheduleItemRequest();
        assertThat(r.isActive()).isTrue();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ScheduleItemRequest validRequest() {
        ScheduleItemRequest r = new ScheduleItemRequest();
        r.setTitle("Math revision");
        r.setDate(LocalDate.of(2026, 6, 10));
        r.setStartTime(LocalTime.of(9, 0));
        r.setEndTime(LocalTime.of(10, 30));
        r.setActive(true);
        return r;
    }

    private Set<String> violationMessages(ScheduleItemRequest r) {
        Set<ConstraintViolation<ScheduleItemRequest>> violations = validator.validate(r);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}
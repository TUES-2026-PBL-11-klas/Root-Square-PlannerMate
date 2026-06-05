package com.rootsquare.planmate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleItemResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String location;
    private boolean repeating;
    private boolean active;

    public ScheduleItemResponse(
            Long id,
            String title,
            String description,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String location,
            boolean repeating,
            boolean active
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.repeating = repeating;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public boolean isActive() {
        return active;
    }
}

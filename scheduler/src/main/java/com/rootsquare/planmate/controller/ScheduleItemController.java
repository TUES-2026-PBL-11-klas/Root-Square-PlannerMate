package com.rootsquare.planmate.controller;

import com.rootsquare.planmate.dto.ScheduleItemRequest;
import com.rootsquare.planmate.dto.ScheduleItemResponse;
import com.rootsquare.planmate.service.CalendarExportService;
import com.rootsquare.planmate.service.ScheduleItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule-items")
public class ScheduleItemController {

    private final ScheduleItemService scheduleItemService;
    private final CalendarExportService calendarExportService;

    public ScheduleItemController(
            ScheduleItemService scheduleItemService,
            CalendarExportService calendarExportService
    ) {
        this.scheduleItemService = scheduleItemService;
        this.calendarExportService = calendarExportService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleItemResponse>> getScheduleItems() {
        return ResponseEntity.ok(scheduleItemService.getScheduleItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleItemResponse> getScheduleItem(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleItemService.getScheduleItem(id));
    }

    @PostMapping
    public ResponseEntity<ScheduleItemResponse> createScheduleItem(
            @Valid @RequestBody ScheduleItemRequest request
    ) {
        ScheduleItemResponse createdItem = scheduleItemService.createScheduleItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleItemResponse> updateScheduleItem(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleItemRequest request
    ) {
        return ResponseEntity.ok(scheduleItemService.updateScheduleItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduleItem(@PathVariable Long id) {
        scheduleItemService.deleteScheduleItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/google-calendar-url")
    public ResponseEntity<Map<String, String>> getGoogleCalendarUrl(@PathVariable Long id) {
        ScheduleItemResponse item = scheduleItemService.getScheduleItem(id);
        String url = calendarExportService.generateGoogleCalendarUrl(item);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/{id}/apple-calendar")
    public ResponseEntity<String> getAppleCalendarFile(@PathVariable Long id) {
        ScheduleItemResponse item = scheduleItemService.getScheduleItem(id);
        String icsContent = calendarExportService.generateAppleCalendarFile(item);
        String filename = calendarExportService.buildIcsFilename(item);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/calendar"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(icsContent);
    }
}

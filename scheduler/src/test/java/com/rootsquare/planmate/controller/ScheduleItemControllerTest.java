package com.rootsquare.planmate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rootsquare.planmate.dto.ScheduleItemRequest;
import com.rootsquare.planmate.dto.ScheduleItemResponse;
import com.rootsquare.planmate.exception.NotFoundException;
import com.rootsquare.planmate.service.CalendarExportService;
import com.rootsquare.planmate.service.ScheduleItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleItemController.class)
class ScheduleItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleItemService scheduleItemService;

    @MockBean
    private CalendarExportService calendarExportService;

    private ObjectMapper objectMapper;
    private ScheduleItemResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleResponse = new ScheduleItemResponse(
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

    // ── GET /api/schedule-items ───────────────────────────────────────────────

    @Test
    void getAll_returns200WithList() throws Exception {
        when(scheduleItemService.getScheduleItems()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/schedule-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Math revision"))
                .andExpect(jsonPath("$[0].location").value("Library"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getAll_returnsEmptyArrayWhenNoItems() throws Exception {
        when(scheduleItemService.getScheduleItems()).thenReturn(List.of());

        mockMvc.perform(get("/api/schedule-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/schedule-items/{id} ──────────────────────────────────────────

    @Test
    void getById_returns200WhenFound() throws Exception {
        when(scheduleItemService.getScheduleItem(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/schedule-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Math revision"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(scheduleItemService.getScheduleItem(99L))
                .thenThrow(new NotFoundException("Schedule item with id 99 was not found"));

        mockMvc.perform(get("/api/schedule-items/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/schedule-items ──────────────────────────────────────────────

    @Test
    void create_returns201WithBody() throws Exception {
        ScheduleItemRequest request = validRequest();
        when(scheduleItemService.createScheduleItem(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Math revision"));
    }

    @Test
    void create_returns400WhenTitleIsMissing() throws Exception {
        ScheduleItemRequest request = validRequest();
        request.setTitle(null);

        mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenDateIsMissing() throws Exception {
        ScheduleItemRequest request = validRequest();
        request.setDate(null);

        mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenEndTimeBeforeStartTime() throws Exception {
        ScheduleItemRequest request = validRequest();
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(9, 0));

        mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/schedule-items/{id} ──────────────────────────────────────────

    @Test
    void update_returns200WhenSuccessful() throws Exception {
        when(scheduleItemService.updateScheduleItem(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/schedule-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(scheduleItemService.updateScheduleItem(eq(99L), any()))
                .thenThrow(new NotFoundException("not found"));

        mockMvc.perform(put("/api/schedule-items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/schedule-items/{id} ───────────────────────────────────────

    @Test
    void delete_returns204WhenSuccessful() throws Exception {
        doNothing().when(scheduleItemService).deleteScheduleItem(1L);

        mockMvc.perform(delete("/api/schedule-items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        doThrow(new NotFoundException("not found")).when(scheduleItemService).deleteScheduleItem(99L);

        mockMvc.perform(delete("/api/schedule-items/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/schedule-items/{id}/google-calendar-url ─────────────────────

    @Test
    void googleCalendarUrl_returns200WithUrl() throws Exception {
        when(scheduleItemService.getScheduleItem(1L)).thenReturn(sampleResponse);
        when(calendarExportService.generateGoogleCalendarUrl(sampleResponse))
                .thenReturn("https://calendar.google.com/calendar/render?action=TEMPLATE");

        mockMvc.perform(get("/api/schedule-items/1/google-calendar-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://calendar.google.com/calendar/render?action=TEMPLATE"));
    }

    // ── GET /api/schedule-items/{id}/apple-calendar ───────────────────────────

    @Test
    void appleCalendar_returns200WithIcsContentType() throws Exception {
        when(scheduleItemService.getScheduleItem(1L)).thenReturn(sampleResponse);
        when(calendarExportService.generateAppleCalendarFile(sampleResponse)).thenReturn("BEGIN:VCALENDAR\r\nEND:VCALENDAR\r\n");
        when(calendarExportService.buildIcsFilename(sampleResponse)).thenReturn("math-revision.ics");

        mockMvc.perform(get("/api/schedule-items/1/apple-calendar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"math-revision.ics\""))
                .andExpect(content().contentTypeCompatibleWith("text/calendar"));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ScheduleItemRequest validRequest() {
        ScheduleItemRequest r = new ScheduleItemRequest();
        r.setTitle("Math revision");
        r.setDescription("Practice equations");
        r.setDate(LocalDate.of(2026, 6, 10));
        r.setStartTime(LocalTime.of(9, 0));
        r.setEndTime(LocalTime.of(10, 30));
        r.setLocation("Library");
        r.setRepeating(false);
        r.setActive(true);
        return r;
    }
}
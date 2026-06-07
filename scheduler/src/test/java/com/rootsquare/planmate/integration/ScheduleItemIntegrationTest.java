package com.rootsquare.planmate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rootsquare.planmate.dto.ScheduleItemRequest;
import com.rootsquare.planmate.repository.ScheduleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full Spring context + H2 integration test for the schedule-items REST API.
 * Requires application.properties to fall back to H2 when no datasource env vars
 * are set, which the scheduler's defaults already support.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.otlp.metrics.export.url=http://localhost:9999",
                "management.otlp.metrics.export.headers.authorization=Basic dGVzdA=="
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScheduleItemRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    void fullCrud_createReadUpdateDelete() throws Exception {
        // CREATE
        String createBody = objectMapper.writeValueAsString(validRequest("Biology lab"));
        MvcResult createResult = mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Biology lab"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        // Extract created ID
        String responseBody = createResult.getResponse().getContentAsString();
        long id = objectMapper.readTree(responseBody).get("id").asLong();

        // READ by ID
        mockMvc.perform(get("/api/schedule-items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Biology lab"));

        // READ list
        mockMvc.perform(get("/api/schedule-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Biology lab"));

        // UPDATE
        ScheduleItemRequest updated = validRequest("Biology lab — updated");
        mockMvc.perform(put("/api/schedule-items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Biology lab — updated"));

        // DELETE
        mockMvc.perform(delete("/api/schedule-items/" + id))
                .andExpect(status().isNoContent());

        // Confirm gone
        mockMvc.perform(get("/api/schedule-items/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withInvalidPayload_returns400() throws Exception {
        mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_returnsItemsSortedByDateThenStartTime() throws Exception {
        mockMvc.perform(post("/api/schedule-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestAt(LocalDate.now().plusDays(2), LocalTime.of(9, 0)))));
        mockMvc.perform(post("/api/schedule-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestAt(LocalDate.now().plusDays(1), LocalTime.of(9, 0)))));

        mockMvc.perform(get("/api/schedule-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value(LocalDate.now().plusDays(1).toString()));
    }

    @Test
    void googleCalendarUrl_returns200WithUrl() throws Exception {
        String body = objectMapper.writeValueAsString(validRequest("Exam prep"));
        MvcResult result = mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/schedule-items/" + id + "/google-calendar-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.containsString("calendar.google.com")));
    }

    @Test
    void appleCalendar_returns200WithIcsBody() throws Exception {
        String body = objectMapper.writeValueAsString(validRequest("Study session"));
        MvcResult result = mockMvc.perform(post("/api/schedule-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        MvcResult icsResult = mockMvc.perform(get("/api/schedule-items/" + id + "/apple-calendar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString(".ics")))
                .andReturn();

        String icsBody = icsResult.getResponse().getContentAsString();
        assertThat(icsBody).contains("BEGIN:VCALENDAR");
        assertThat(icsBody).contains("SUMMARY:Study session");
    }

    @Test
    void deleteNonExistentItem_returns404() throws Exception {
        mockMvc.perform(delete("/api/schedule-items/99999"))
                .andExpect(status().isNotFound());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ScheduleItemRequest validRequest(String title) {
        return requestAt(title, LocalDate.now().plusDays(1), LocalTime.of(9, 0));
    }

    private ScheduleItemRequest requestAt(LocalDate date, LocalTime start) {
        return requestAt("Test item", date, start);
    }

    private ScheduleItemRequest requestAt(String title, LocalDate date, LocalTime start) {
        ScheduleItemRequest r = new ScheduleItemRequest();
        r.setTitle(title);
        r.setDescription("A description");
        r.setDate(date);
        r.setStartTime(start);
        r.setEndTime(start.plusHours(1));
        r.setLocation("Room A");
        r.setRepeating(false);
        r.setActive(true);
        return r;
    }
}
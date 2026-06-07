package com.rootsquare.planmate.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the GlobalExceptionHandler by triggering real exceptions through a tiny
 * test-only controller that is wired in via @WebMvcTest.
 */
@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── NotFoundException → 404 ───────────────────────────────────────────────

    @Test
    void notFound_returns404WithStructuredBody() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Item not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── IllegalArgumentException → 400 ───────────────────────────────────────

    @Test
    void illegalArgument_returns400() throws Exception {
        mockMvc.perform(get("/test/bad-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad argument"));
    }

    // ── Validation → 400 with field errors ───────────────────────────────────

    @Test
    void validationFailure_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))  // missing required fields
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    // ── Unexpected exception → 500 ────────────────────────────────────────────

    @Test
    void unexpectedException_returns500() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected server error"));
    }

    // ── Minimal inner controller to drive handler scenarios ──────────────────

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new NotFoundException("Item not found");
        }

        @GetMapping("/bad-argument")
        public void throwBadArg() {
            throw new IllegalArgumentException("Bad argument");
        }

        @PostMapping("/validate")
        public void throwValidation(
                @org.springframework.validation.annotation.Validated
                @org.springframework.web.bind.annotation.RequestBody ValidatableBody body) {
        }

        @GetMapping("/boom")
        public void throwUnexpected() throws Exception {
            throw new Exception("Unexpected");
        }

        // Simple bean with a required field to drive MethodArgumentNotValidException
        record ValidatableBody(
                @jakarta.validation.constraints.NotBlank(message = "name is required")
                String name
        ) {}
    }
}
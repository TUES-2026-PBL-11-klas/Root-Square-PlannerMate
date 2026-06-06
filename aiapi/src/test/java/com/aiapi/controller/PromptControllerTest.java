package com.aiapi.controller;

import com.aiapi.model.PromptRequest;
import com.aiapi.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import com.aiapi.model.PromptResponse;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class PromptControllerTest {

    private StubAiService aiService;
    private PromptController controller;

    @BeforeEach
    void setUp() {
        aiService = new StubAiService();
        controller = new PromptController(aiService);
    }

    @Test
    void shouldReturnResponseForValidPrompt() {
        aiService.response = "Black holes are regions where gravity is so strong nothing can escape.";
        PromptRequest request = new PromptRequest();
        request.setPrompt("Explain black holes.");

        ResponseEntity<PromptResponse> result = controller.complete(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResponse()).isEqualTo(aiService.response);
        assertThat(aiService.calls).isEqualTo(1);
        assertThat(aiService.lastPrompt).isEqualTo("Explain black holes.");
    }

    @Test
    void shouldReturnBadRequestForNullPrompt() {
        PromptRequest request = new PromptRequest();
        request.setPrompt(null);

        ResponseEntity<PromptResponse> result = controller.complete(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThat(aiService.calls).isZero();
    }

    @Test
    void shouldReturnBadRequestForBlankPrompt() {
        PromptRequest request = new PromptRequest();
        request.setPrompt("   ");

        ResponseEntity<PromptResponse> result = controller.complete(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
        assertThat(aiService.calls).isZero();
    }

    // * Lightweight stub — avoids Spring context and WebClient setup.
    private static class StubAiService extends AiService {
        int calls;
        String lastPrompt;
        String response = "";

        StubAiService() {
            super(WebClient.builder());
        }

        @Override
        public String complete(String prompt) {
            calls++;
            lastPrompt = prompt;
            return response;
        }
    }
}
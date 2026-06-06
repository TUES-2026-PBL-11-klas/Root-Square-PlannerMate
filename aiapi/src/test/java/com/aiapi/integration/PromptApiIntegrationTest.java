package com.aiapi.integration;

import com.aiapi.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PromptApiIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AiService aiService;

    @Test
    void healthEndpointShouldBeUp() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void postShouldReturnResponseForValidPrompt() {
        stub().reset();
        stub().response = "Black holes are very dense.";

        webTestClient.post()
                .uri("/api/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"prompt\":\"Explain black holes.\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.response").isEqualTo("Black holes are very dense.");

        assertThat(stub().calls).isEqualTo(1);
        assertThat(stub().lastPrompt).isEqualTo("Explain black holes.");
    }

    @Test
    void postShouldReturnBadRequestForBlankPrompt() {
        stub().reset();

        webTestClient.post()
                .uri("/api/ai")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"prompt\":\"   \"}")
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(stub().calls).isZero();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AiService aiService() {
            return new StubAiService();
        }
    }

    static class StubAiService extends AiService {
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

        void reset() {
            calls = 0;
            lastPrompt = null;
            response = "";
        }
    }

    private StubAiService stub() {
        return (StubAiService) aiService;
    }
}
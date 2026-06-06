package com.aiapi.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceIntegrationTest {

    private MockWebServer mockWebServer;
    private AiService aiService;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        aiService = new AiService(WebClient.builder());

        WebClient mockClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/openai/v1/chat/completions").toString())
                .build();

        ReflectionTestUtils.setField(aiService, "webClient", mockClient);
        ReflectionTestUtils.setField(aiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "model", "test-model");
        ReflectionTestUtils.setField(aiService, "maxTokens", 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldCallGroqApiAndReturnRawText() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"choices\":[{\"message\":{\"content\":\"Hello, world!\"}}]}"));

        String result = aiService.complete("Say hello.");

        assertThat(result).isEqualTo("Hello, world!");

        RecordedRequest recorded = mockWebServer.takeRequest();
        assertThat(recorded.getMethod()).isEqualTo("POST");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer test-api-key");
        assertThat(recorded.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(recorded.getBody().readUtf8())
                .contains("\"model\":\"test-model\"")
                .contains("Say hello.");
    }

    @Test
    void shouldThrowOnApiError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"error\":\"unauthorized\"}"));

        assertThatThrownBy(() -> aiService.complete("hello"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Groq API error");
    }

    @Test
    void shouldReturnEmptyStringWhenChoicesAreEmpty() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"choices\":[]}"));

        String result = aiService.complete("hello");

        assertThat(result).isEmpty();
    }
}
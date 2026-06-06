package com.aiapi.service;

import com.aiapi.model.GroqRequest;
import com.aiapi.model.GroqRequest.Message;
import com.aiapi.model.GroqResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.max-tokens:1024}")
    private int maxTokens;

    private final WebClient webClient;

    public AiService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(GROQ_API_URL).build();
    }

    public String complete(String prompt) {
        GroqRequest request = new GroqRequest(
                model,
                maxTokens,
                List.of(new Message("user", prompt))
        );

        log.info("Sending prompt to Groq API using model: {}", model);

        GroqResponse response = webClient.post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Groq API error - Status: {}, Body: {}", clientResponse.statusCode(), errorBody);
                            return Mono.error(new RuntimeException(
                                    "Groq API error " + clientResponse.statusCode() + ": " + errorBody));
                        })
                )
                .bodyToMono(GroqResponse.class)
                .block();

        if (response == null) {
            throw new RuntimeException("No response from Groq API");
        }

        String text = response.firstText();
        log.info("Groq response received ({} chars)", text.length());
        return text;
    }
}
package com.rootsquare.planmate.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    private final RestClient restClient;
    private final String aiBaseUrl;

    public AiProxyController(
            RestClient.Builder restClientBuilder,
            @Value("${ai.service.base-url:http://localhost:8082}") String aiBaseUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.aiBaseUrl = trimTrailingSlash(aiBaseUrl);
    }

    @PostMapping
    public ResponseEntity<String> complete(@RequestBody Map<String, Object> body) {
        try {
            return restClient.post()
                    .uri(aiBaseUrl + "/api/ai")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException exception) {
            return ResponseEntity
                    .status(exception.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(exception.getResponseBodyAsString());
        } catch (ResourceAccessException exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"message\":\"AI service is unavailable\"}");
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}

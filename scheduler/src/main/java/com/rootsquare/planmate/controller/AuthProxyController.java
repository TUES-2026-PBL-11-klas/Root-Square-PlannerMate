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
@RequestMapping("/api/auth")
public class AuthProxyController {

    private final RestClient restClient;
    private final String iamBaseUrl;

    public AuthProxyController(
            RestClient.Builder restClientBuilder,
            @Value("${iam.service.base-url:http://localhost:8085}") String iamBaseUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.iamBaseUrl = trimTrailingSlash(iamBaseUrl);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, Object> body) {
        return forwardToIam("/api/auth/login", body);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, Object> body) {
        return forwardToIam("/api/auth/register", body);
    }

    private ResponseEntity<String> forwardToIam(String path, Map<String, Object> body) {
        try {
            return restClient.post()
                    .uri(iamBaseUrl + path)
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
                    .body("{\"message\":\"IAM service is unavailable\"}");
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}

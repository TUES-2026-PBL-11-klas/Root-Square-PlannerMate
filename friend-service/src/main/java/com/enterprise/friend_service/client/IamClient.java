package com.enterprise.friend_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// ? @Component makes this injectable as a Spring bean.
// ? RestClient (Spring 6.1+) is the modern replacement for RestTemplate.
@Component
@RequiredArgsConstructor
public class IamClient {

    private final RestClient iamRestClient;

    // * Fetches a user by their UUID from the IAM internal endpoint.
    // ! The friend service forwards the caller's JWT so the IAM service
    // ! can authenticate the request — no service-to-service secret needed.
    public IamUserResponse getUserById(UUID userId, String bearerToken) {
        try {
            return iamRestClient.get()
                    .uri("/api/internal/users/{id}", userId)
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .body(IamUserResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found in IAM service: " + userId
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "IAM service unavailable. Please try again later."
            );
        }
    }

    // * Fetches a user by email — used to resolve the caller's own UUID from their JWT subject.
    // ! We need the UUID because the friends table stores UUIDs, not emails.
    public IamUserResponse getUserByEmail(String email, String bearerToken) {
        try {
            // * The IAM /api/users/me endpoint returns the profile of whoever owns the JWT.
            return iamRestClient.get()
                    .uri("/api/users/me")
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .body(IamUserResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found in IAM service: " + email
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "IAM service unavailable. Please try again later."
            );
        }
    }
}

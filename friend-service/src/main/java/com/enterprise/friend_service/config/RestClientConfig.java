package com.enterprise.friend_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient iamRestClient(
            RestClient.Builder builder,
            @Value("${iam.service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}

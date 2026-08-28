package com.interview.lottory.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Component
public class UserServiceClient {
    private final RestClient restClient;

    public UserServiceClient(
            @Value("${app.user-service.base-url}") String baseUrl,
            @Value("${app.user-service.http2-enabled:true}") boolean http2Enabled) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(http2Enabled ? HttpClient.Version.HTTP_2 : HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public CurrentUser getOrCreateCurrentUser(String bearerToken) {
        UserApiResponse<CurrentUser> response = restClient.get()
                .uri("/api/users/me")
                .headers(headers -> headers.setBearerAuth(bearerToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalStateException("User service did not return the current user");
        }
        return response.data();
    }

    private record UserApiResponse<T>(boolean success, T data) {
    }
}

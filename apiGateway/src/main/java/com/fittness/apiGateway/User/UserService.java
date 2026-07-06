package com.fittness.apiGateway.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final WebClient userServiceWebClient;
    public String testConnection() {
        return userServiceWebClient.get()
                .uri("/api/user/ping")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
    public Mono<Boolean> validateUser(String userId) {
        return userServiceWebClient.get()
                .uri("http://userservice/api/user/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new RuntimeException("User not found: " + userId));
                    } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("InvalidRequest: " + userId));
                    }
                    return Mono.error(new RuntimeException("Unexpected error: " + userId));
                });
    }


    public Mono<UserResponse> registerUser(registerRequest request) {
        log.info("calling user Registration API");

        return userServiceWebClient.post()
                .uri("http://userservice/api/user/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("InvalidRequest: " + e.getMessage()));
                    } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        return Mono.error(new RuntimeException("server error: " + e.getMessage()));
                    }
                    return Mono.error(new RuntimeException("Unexpected error: " + e.getMessage()));
                });
    }
}
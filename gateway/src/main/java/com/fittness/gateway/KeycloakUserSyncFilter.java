package com.fittness.gateway;

import com.fittness.gateway.User.UserService;
import com.fittness.gateway.User.registerRequest;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String userId = exchange.getRequest().getHeaders().getFirst("X-HEADER-ID");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        // Extract token details safely if token is present
        registerRequest RegReq = null;
        if (token != null) {
            RegReq = userDetails(token);
        }

        // Fallback to Keycloak ID if X-HEADER-ID is missing
        if (userId == null && RegReq != null) {
            userId = RegReq.getKeykloakId();
        }

        if (userId != null && token != null) {

            String finalUserId = userId;
            registerRequest finalRegReq = RegReq;

            log.info("[Filter] Checking if user exists in database for ID: {}", finalUserId);

            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist) {
                            if (finalRegReq != null) {
                                log.info("[Filter] User {} not found. Triggering registration...", finalUserId);

                                return userService.registeruser(finalRegReq)
                                        .doOnSuccess(result -> log.info("[Filter] Successfully registered user in DB: {}", finalUserId))
                                        .doOnError(error -> log.error("[Filter] Failed to save user in DB: {}", finalUserId, error))
                                        .then(); // Converts Mono<User> to Mono<Void> to match the flatMap expectations
                            } else {
                                log.warn("[Filter] Cannot register user. Token claims could not be parsed.");
                                return Mono.empty();
                            }
                        } else {
                            log.info("[Filter] User {} already exists in database.", finalUserId);
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> {
                        // This executes ONLY after the flatMap (validation & optional registration) completes successfully
                        log.info("[Filter] Forwarding request to downstream services with X-User-ID: {}", finalUserId);

                        ServerHttpRequest mutatedRequest = exchange.getRequest()
                                .mutate()
                                .header("X-User-ID", finalUserId)
                                .build();

                        return chain.filter(
                                exchange.mutate()
                                        .request(mutatedRequest)
                                        .build()
                        );
                    }));
        }

        log.warn("[Filter] Missing userId or token. Skipping sync logic.");
        return chain.filter(exchange);
    }

    private registerRequest userDetails(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }

            String tokenWithoutBearer = token.replace("Bearer", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            registerRequest registerRequest = new registerRequest();
            registerRequest.setEmail(claims.getClaimAsString("email"));
            registerRequest.setKeykloakId(claims.getClaimAsString("sub"));
            registerRequest.setPassword(claims.getClaimAsString("dummy"));
            registerRequest.setFirstname(claims.getClaimAsString("given_name"));
            registerRequest.setLastName(claims.getClaimAsString("family_name"));
            return registerRequest;

        } catch (Exception e) {
            log.error("[Filter] Error parsing JWT Token claims", e);
            return null;
        }
    }
}
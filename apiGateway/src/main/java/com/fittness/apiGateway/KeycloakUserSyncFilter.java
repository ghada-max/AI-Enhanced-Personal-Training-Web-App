package com.fittness.apiGateway;


import com.fittness.apiGateway.User.UserService;
import com.fittness.apiGateway.User.registerRequest;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
@ConditionalOnProperty(
        name = "filter.keycloak.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@Slf4j
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userservice;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 1. Extraction des headers
        String userId = exchange.getRequest().getHeaders().getFirst("User-Id");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        registerRequest registerrequest = getUserDetails(token);

        if (userId == null && registerrequest != null) {
            userId = registerrequest.getKeykloakId();
        }

        final String finalUserId = userId;

        if (finalUserId != null && token != null) {
            return userservice.validateUser(finalUserId)
                    .flatMap(exist -> {
                        if (!exist) {

                            if (registerrequest != null) {
                                return userservice.registerUser(registerrequest).then();
                            } else {
                                return Mono.empty();
                            }

                        } else {
                            log.info("User already exists. Skip sync.");
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-USER-ID", finalUserId)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }

        // Si userId ou token est nul, on laisse passer la requête normalement
        return chain.filter(exchange);
    }

    private registerRequest getUserDetails(String token) {

        try{

            String tokenWithoutBearer=token.replace("Bearer","").trim();
            SignedJWT signedJWT= SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims=signedJWT.getJWTClaimsSet();
            registerRequest registerrequest=new registerRequest();
            registerrequest.setEmail(claims.getStringClaim("email"));
            registerrequest.setKeykloakId(claims.getStringClaim("sub"));
            registerrequest.setPassword("dummy@123123");
            registerrequest.setFirstname(claims.getStringClaim("given_name"));
            registerrequest.setLastName(claims.getStringClaim("family_name"));
            return registerrequest;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

}

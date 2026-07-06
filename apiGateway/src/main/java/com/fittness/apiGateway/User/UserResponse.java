package com.fittness.apiGateway.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Data
public class UserResponse {


    private String id;
    private String keykloakId;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}

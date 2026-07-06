package com.fittness.apiGateway.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class registerRequest {

    @NotBlank(message="Email is required")
    @Email(message="Invalid email format")
    private String email;
    private String keykloakId;

    private String password;
    private String firstname;
    private String LastName;
}

package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username, email, or employee ID is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}

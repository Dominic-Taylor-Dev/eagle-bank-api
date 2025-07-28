package com.eaglebank.auth.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}
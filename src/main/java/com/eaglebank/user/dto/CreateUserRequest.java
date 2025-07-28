package com.eaglebank.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format starting with '+' followed by country code and number")
        String phoneNumber,

        @NotNull(message = "Address is required")
        @Valid
        AddressDto address
) {}

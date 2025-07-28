package com.eaglebank.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
        @NotBlank(message = "Address line 1 is required")
        String line1,
        String line2,
        String line3,
        @NotBlank(message = "Town is required")
        String town,
        @NotBlank(message = "County is required")
        String county,
        @NotBlank(message = "Postcode is required")
        String postcode
) {}

package com.eaglebank.auth.dto;

public record AuthResponse(
        String token,
        String tokenType
) {}

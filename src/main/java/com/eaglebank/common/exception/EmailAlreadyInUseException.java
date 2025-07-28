package com.eaglebank.common.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyInUseException extends RuntimeException {
    private final String email;

    public EmailAlreadyInUseException(String email) {
        super(String.format("Email already in use: %s", email));
        this.email = email;
    }
}

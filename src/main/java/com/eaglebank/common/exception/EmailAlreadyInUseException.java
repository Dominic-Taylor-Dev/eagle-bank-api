package com.eaglebank.common.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super(String.format("Email already in use: %s", email));
    }
}

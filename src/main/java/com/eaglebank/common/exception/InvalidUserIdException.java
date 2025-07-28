package com.eaglebank.common.exception;

public class InvalidUserIdException extends RuntimeException {
    public InvalidUserIdException() {
        super("User ID not found");
    }
}

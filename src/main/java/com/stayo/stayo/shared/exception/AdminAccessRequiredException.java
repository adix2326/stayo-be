package com.stayo.stayo.shared.exception;

public class AdminAccessRequiredException extends RuntimeException {
    public AdminAccessRequiredException(String message) {
        super(message);
    }
}

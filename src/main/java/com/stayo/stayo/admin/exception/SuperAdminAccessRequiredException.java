package com.stayo.stayo.admin.exception;

public class SuperAdminAccessRequiredException extends RuntimeException {
    public SuperAdminAccessRequiredException(String message) {
        super(message);
    }
}

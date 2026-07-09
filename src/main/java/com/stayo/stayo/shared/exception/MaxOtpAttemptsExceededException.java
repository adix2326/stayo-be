package com.stayo.stayo.shared.exception;

public class MaxOtpAttemptsExceededException extends RuntimeException{
    public MaxOtpAttemptsExceededException(String message) {
        super(message);
    }
}
